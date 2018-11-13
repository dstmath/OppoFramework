package android.util;

import android.graphics.Path;

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
public class PathParser {
    static final String LOGTAG = null;

    public static class PathData {
        long mNativePathData;

        public PathData() {
            this.mNativePathData = 0;
            this.mNativePathData = PathParser.nCreateEmptyPathData();
        }

        public PathData(PathData data) {
            this.mNativePathData = 0;
            this.mNativePathData = PathParser.nCreatePathData(data.mNativePathData);
        }

        public PathData(String pathString) {
            this.mNativePathData = 0;
            this.mNativePathData = PathParser.nCreatePathDataFromString(pathString, pathString.length());
            if (this.mNativePathData == 0) {
                throw new IllegalArgumentException("Invalid pathData: " + pathString);
            }
        }

        public long getNativePtr() {
            return this.mNativePathData;
        }

        public void setPathData(PathData source) {
            PathParser.nSetPathData(this.mNativePathData, source.mNativePathData);
        }

        protected void finalize() throws Throwable {
            if (this.mNativePathData != 0) {
                PathParser.nFinalize(this.mNativePathData);
                this.mNativePathData = 0;
            }
            super.finalize();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.util.PathParser.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.util.PathParser.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.PathParser.<clinit>():void");
    }

    private static native boolean nCanMorph(long j, long j2);

    private static native long nCreateEmptyPathData();

    private static native long nCreatePathData(long j);

    private static native long nCreatePathDataFromString(String str, int i);

    private static native void nCreatePathFromPathData(long j, long j2);

    private static native void nFinalize(long j);

    private static native boolean nInterpolatePathData(long j, long j2, long j3, float f);

    private static native void nParseStringForPath(long j, String str, int i);

    private static native void nSetPathData(long j, long j2);

    public static Path createPathFromPathData(String pathString) {
        if (pathString == null) {
            throw new IllegalArgumentException("Path string can not be null.");
        }
        Path path = new Path();
        nParseStringForPath(path.mNativePath, pathString, pathString.length());
        return path;
    }

    public static void createPathFromPathData(Path outPath, PathData data) {
        nCreatePathFromPathData(outPath.mNativePath, data.mNativePathData);
    }

    public static boolean canMorph(PathData pathDataFrom, PathData pathDataTo) {
        return nCanMorph(pathDataFrom.mNativePathData, pathDataTo.mNativePathData);
    }

    public static boolean interpolatePathData(PathData outData, PathData fromData, PathData toData, float fraction) {
        return nInterpolatePathData(outData.mNativePathData, fromData.mNativePathData, toData.mNativePathData, fraction);
    }
}
