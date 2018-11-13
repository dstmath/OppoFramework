package android.media;

import android.app.ActivityThread;
import android.app.backup.FullBackup;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.zip.Adler32;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class MiniThumbFile {
    private static final long AUTHOR = -1058094635;
    public static final int BYTES_PER_MINTHUMB = 16384;
    private static final int DATA_START_OFFSET = 524288;
    private static final int IH_DATA_CHECKSUM_OFFSET = 16;
    private static final int IH_LENGTH_OFFSET = 28;
    private static final int IH_MAGIC_OFFSET = 8;
    private static final int IH_ORIGINAL_ID_OFFSET = 0;
    private static final int IH_POSITION_OFFSET = 24;
    private static final int INDEX_HEADER_SIZE = 32;
    private static final long MAGIC_THUMB_FILE = 538182168;
    private static final int MAX_THUMB_COUNT_PER_FILE = 16383;
    private static final int MAX_THUMB_FILE_SIZE = 52428800;
    private static final int MINI_THUMB_DATA_FILE_VERSION = 6;
    private static final String TAG = "MiniThumbFile";
    private static final int VERSION_HEADER_SIZE = 32;
    private static final int VH_ACTIVECOUNT_OFFSET = 12;
    private static final int VH_AUTHOR_OFFSET = 16;
    private static final int VH_CHECKSUM_OFFSET = 24;
    private static final int VH_MAGIC_OFFSET = 4;
    private static final int VH_VERSION_OFFSET = 0;
    private static Object sLock;
    private static final HashMap<Long, MiniThumbDataFile> sMiniThumbDataFile = null;
    private static MiniThumbFile sMiniThumbFile;
    private ByteBuffer mBuffer;
    private Adler32 mChecker;
    private byte[] mIndexHeader;
    private Uri mUri;
    private byte[] mVersionHeader;

    private class MiniThumbDataFile {
        private int mActiveCount;
        private FileChannel mChannel;
        private MappedByteBuffer mIndexMappedBuffer;
        private String mPath;
        private RandomAccessFile mRandomAccessFile;
        final /* synthetic */ MiniThumbFile this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.MiniThumbFile.MiniThumbDataFile.<init>(android.media.MiniThumbFile, java.io.RandomAccessFile, java.lang.String):void, dex: 
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
        public MiniThumbDataFile(android.media.MiniThumbFile r1, java.io.RandomAccessFile r2, java.lang.String r3) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.MiniThumbFile.MiniThumbDataFile.<init>(android.media.MiniThumbFile, java.io.RandomAccessFile, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.<init>(android.media.MiniThumbFile, java.io.RandomAccessFile, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.load():boolean, dex: 
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
        private synchronized boolean load() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.load():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.load():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MiniThumbFile.MiniThumbDataFile.reset():void, dex: 
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
        private synchronized void reset() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MiniThumbFile.MiniThumbDataFile.reset():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.reset():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MiniThumbFile.MiniThumbDataFile.bufferToString(byte[]):java.lang.String, dex: 
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
        public java.lang.String bufferToString(byte[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MiniThumbFile.MiniThumbDataFile.bufferToString(byte[]):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.bufferToString(byte[]):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MiniThumbFile.MiniThumbDataFile.close():void, dex: 
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
        public synchronized void close() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MiniThumbFile.MiniThumbDataFile.close():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.close():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.getActiveCount():int, dex: 
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
        public synchronized int getActiveCount() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.getActiveCount():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.getActiveCount():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.getDataFromThumbFile(byte[], long, android.media.MiniThumbFile$ThumbResult):byte[], dex: 
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
        public synchronized byte[] getDataFromThumbFile(byte[] r1, long r2, android.media.MiniThumbFile.ThumbResult r4) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.getDataFromThumbFile(byte[], long, android.media.MiniThumbFile$ThumbResult):byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.getDataFromThumbFile(byte[], long, android.media.MiniThumbFile$ThumbResult):byte[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.getIndexHeader(byte[], long):java.nio.ByteBuffer, dex: 
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
        public synchronized java.nio.ByteBuffer getIndexHeader(byte[] r1, long r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.getIndexHeader(byte[], long):java.nio.ByteBuffer, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.getIndexHeader(byte[], long):java.nio.ByteBuffer");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.getMagic(long):long, dex: 
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
        public synchronized long getMagic(long r1) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.getMagic(long):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.getMagic(long):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MiniThumbFile.MiniThumbDataFile.syncAll():void, dex: 
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
        public void syncAll() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MiniThumbFile.MiniThumbDataFile.syncAll():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.syncAll():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.syncIndex():void, dex: 
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
        public void syncIndex() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.syncIndex():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.syncIndex():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.MiniThumbFile.MiniThumbDataFile.updateActiveCount():int, dex: 
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
        public synchronized int updateActiveCount() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.MiniThumbFile.MiniThumbDataFile.updateActiveCount():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.updateActiveCount():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MiniThumbFile.MiniThumbDataFile.updateDataToThumbFile(byte[], long, long):void, dex: 
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
        public synchronized void updateDataToThumbFile(byte[] r1, long r2, long r4) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MiniThumbFile.MiniThumbDataFile.updateDataToThumbFile(byte[], long, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.updateDataToThumbFile(byte[], long, long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.updateIndexHeader(byte[], long):void, dex: 
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
        public synchronized void updateIndexHeader(byte[] r1, long r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.MiniThumbFile.MiniThumbDataFile.updateIndexHeader(byte[], long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.MiniThumbDataFile.updateIndexHeader(byte[], long):void");
        }
    }

    public static class ThumbResult {
        public static final int SUCCESS = 2;
        public static final int UNSPECIFIED = 0;
        public static final int WRONG_CHECK_CODE = 1;
        private int mDetail;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.media.MiniThumbFile.ThumbResult.<init>():void, dex: 
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
        public ThumbResult() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.media.MiniThumbFile.ThumbResult.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.ThumbResult.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.media.MiniThumbFile.ThumbResult.getDetail():int, dex: 
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
        public int getDetail() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.media.MiniThumbFile.ThumbResult.getDetail():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.ThumbResult.getDetail():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.media.MiniThumbFile.ThumbResult.setDetail(int):void, dex: 
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
        void setDetail(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.media.MiniThumbFile.ThumbResult.setDetail(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.ThumbResult.setDetail(int):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MiniThumbFile.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MiniThumbFile.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MiniThumbFile.<clinit>():void");
    }

    public static synchronized void reset() {
        synchronized (MiniThumbFile.class) {
            if (sMiniThumbFile != null) {
                sMiniThumbFile.deactivate();
            }
            sMiniThumbFile = null;
        }
    }

    public static synchronized MiniThumbFile instance(Uri uri) {
        MiniThumbFile miniThumbFile;
        synchronized (MiniThumbFile.class) {
            if (sMiniThumbFile == null) {
                sMiniThumbFile = new MiniThumbFile(null);
            }
            miniThumbFile = sMiniThumbFile;
        }
        return miniThumbFile;
    }

    private static String randomAccessFilePath(long id) {
        String storagePath = Environment.getExternalStorageDirectory().getPath();
        String directoryName = getMiniThumbFileDirectoryPath();
        return storagePath + "/" + directoryName + "/" + (getMiniThumbFilePrefix() + (((int) id) / MAX_THUMB_COUNT_PER_FILE));
    }

    private MiniThumbDataFile miniThumbDataFile(long id) {
        MiniThumbDataFile miniThumbDataFile;
        synchronized (sLock) {
            long fileIndex = id / 16383;
            miniThumbDataFile = (MiniThumbDataFile) sMiniThumbDataFile.get(Long.valueOf(fileIndex));
            if (miniThumbDataFile == null) {
                String path = randomAccessFilePath(id);
                File directory = new File(path).getParentFile();
                if (!(directory.isDirectory() || directory.mkdirs())) {
                    Log.e(TAG, "Unable to create .thumbnails directory " + directory.toString());
                }
                File file = new File(path);
                try {
                    miniThumbDataFile = new MiniThumbDataFile(this, new RandomAccessFile(file, "rw"), path);
                } catch (IOException ex) {
                    Log.e(TAG, "miniThumbDataFile: IOException(rw) for: " + path + ", try read only mode", ex);
                    try {
                        miniThumbDataFile = new MiniThumbDataFile(this, new RandomAccessFile(file, FullBackup.ROOT_TREE_TOKEN), path);
                    } catch (IOException ex2) {
                        Log.e(TAG, "miniThumbDataFile: IOException(r) for: " + path, ex2);
                    }
                }
                if (miniThumbDataFile != null) {
                    sMiniThumbDataFile.put(Long.valueOf(fileIndex), miniThumbDataFile);
                }
            }
        }
        return miniThumbDataFile;
    }

    public MiniThumbFile(Uri uri) {
        this.mVersionHeader = new byte[32];
        this.mIndexHeader = new byte[32];
        this.mChecker = new Adler32();
        this.mUri = uri;
        this.mBuffer = ByteBuffer.allocateDirect(16384);
        Log.v(TAG, "activate MiniThumbFile " + this);
    }

    public synchronized void deactivate() {
        synchronized (sLock) {
            Iterator<Entry<Long, MiniThumbDataFile>> iterator = sMiniThumbDataFile.entrySet().iterator();
            while (iterator.hasNext()) {
                MiniThumbDataFile miniThumbDataFile = (MiniThumbDataFile) ((Entry) iterator.next()).getValue();
                if (miniThumbDataFile != null) {
                    miniThumbDataFile.close();
                }
                iterator.remove();
            }
        }
        Log.v(TAG, "deactivate MiniThumbFile " + this);
    }

    public synchronized long getMagic(long id) {
        MiniThumbDataFile miniThumbDataFile = miniThumbDataFile(id);
        if (miniThumbDataFile != null) {
            try {
                return miniThumbDataFile.getMagic(id);
            } catch (IOException ex) {
                Log.v(TAG, "Got exception checking file magic: ", ex);
            } catch (RuntimeException ex2) {
                Log.e(TAG, "Got exception when reading magic, id = " + id + ", disk full or mount read-only? " + ex2.getClass());
            }
        }
        return 0;
    }

    public synchronized void saveMiniThumbToFile(byte[] data, long id, long magic) throws IOException {
        MiniThumbDataFile miniThumbDataFile = miniThumbDataFile(id);
        if (miniThumbDataFile != null) {
            try {
                Log.v(TAG, "saveMiniThumbToFile with : id = " + id + ", magic = " + magic);
                miniThumbDataFile.updateDataToThumbFile(data, id, magic);
            } catch (IOException ex) {
                Log.e(TAG, "couldn't save mini thumbnail data for " + id + "; ", ex);
                throw ex;
            } catch (RuntimeException ex2) {
                Log.e(TAG, "couldn't save mini thumbnail data for " + id + "; disk full or mount read-only? " + ex2.getClass());
            }
        } else {
            return;
        }
    }

    public synchronized byte[] getMiniThumbFromFile(long id, byte[] data) {
        return getMiniThumbFromFile(id, data, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0063 A:{Catch:{ IOException -> 0x0083, RuntimeException -> 0x0034 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized byte[] getMiniThumbFromFile(long id, byte[] data, ThumbResult result) {
        boolean needRetry;
        MiniThumbDataFile miniThumbDataFile = miniThumbDataFile(id);
        int retryCount = 0;
        if (miniThumbDataFile == null) {
            return null;
        }
        try {
            Log.v(TAG, "getMiniThumbFromFile for id " + id);
            return miniThumbDataFile.getDataFromThumbFile(data, id, result);
        } catch (IOException ex) {
            Log.w(TAG, "got exception when reading thumbnail id=" + id + ", exception: " + ex);
            needRetry = true;
            if (needRetry) {
                String packageName = ActivityThread.currentOpPackageName();
                if (packageName == null || !packageName.equals("com.instagram.android")) {
                    return null;
                }
                while (needRetry && retryCount <= 2) {
                    retryCount++;
                    MiniThumbDataFile miniThumbDataFile1 = miniThumbDataFile(id);
                    if (miniThumbDataFile1 == null) {
                        return null;
                    }
                    try {
                        Log.v(TAG, retryCount + " getMiniThumbFromFile for id " + id);
                        return miniThumbDataFile1.getDataFromThumbFile(data, id, result);
                    } catch (IOException ex1) {
                        Log.w(TAG, retryCount + " got exception when reading thumbnail id=" + id + ", exception: " + ex1);
                        needRetry = true;
                    } catch (RuntimeException ex12) {
                        Log.e(TAG, retryCount + " Got exception when reading thumbnail, id = " + id + ", disk full or mount read-only? " + ex12.getClass());
                        needRetry = true;
                    }
                }
            }
            return null;
        } catch (RuntimeException ex2) {
            Log.e(TAG, "Got exception when reading thumbnail, id = " + id + ", disk full or mount read-only? " + ex2.getClass());
            needRetry = true;
            if (needRetry) {
            }
            return null;
        }
    }

    public static String getMiniThumbFilePrefix() {
        return ".thumbdata-6.0_";
    }

    public static String getMiniThumbFileDirectoryPath() {
        return ".thumbnails";
    }

    private int readInt(byte[] buf, int offset) {
        return (((buf[offset] & 255) | ((buf[offset + 1] & 255) << 8)) | ((buf[offset + 2] & 255) << 16)) | ((buf[offset + 3] & 255) << 24);
    }

    private long readLong(byte[] buf, int offset) {
        long result = (long) (buf[offset + 7] & 255);
        for (int i = 6; i >= 0; i--) {
            result = (result << 8) | ((long) (buf[offset + i] & 255));
        }
        return result;
    }

    private void writeInt(byte[] buf, int offset, int value) {
        for (int i = 0; i < 4; i++) {
            buf[offset + i] = (byte) (value & 255);
            value >>= 8;
        }
    }

    private void writeLong(byte[] buf, int offset, long value) {
        for (int i = 0; i < 8; i++) {
            buf[offset + i] = (byte) ((int) (255 & value));
            value >>= 8;
        }
    }

    private long checkSum(byte[] data) {
        this.mChecker.reset();
        this.mChecker.update(data);
        return this.mChecker.getValue();
    }

    private long checkSum(byte[] data, int offset, int length) {
        this.mChecker.reset();
        this.mChecker.update(data, offset, length);
        return this.mChecker.getValue();
    }
}
