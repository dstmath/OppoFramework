package android.graphics;

public abstract class OppoBaseRenderNode {
    public static final int USAGE_COLORBITMAPFORCEINVERT = 3;
    public static final int USAGE_FOREGROUND = 2;

    private static native void nSetUsageForceDarkAlgorithmType(long j, int i);

    /* access modifiers changed from: protected */
    public abstract long getNativeRenderNode();

    public abstract void setUsageHint(int i);

    public void setUsageForceDarkAlgorithmType(int algorithmType) {
        nSetUsageForceDarkAlgorithmType(getNativeRenderNode(), algorithmType);
    }
}
