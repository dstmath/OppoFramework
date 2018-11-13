package android.view;

public final class SurfaceSession {
    private long mNativeClient = nativeCreate();

    private static native long nativeCreate();

    private static native void nativeDestroy(long j);

    private static native void nativeKill(long j);

    protected void finalize() throws Throwable {
        try {
            if (this.mNativeClient != 0) {
                nativeDestroy(this.mNativeClient);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public void kill() {
        nativeKill(this.mNativeClient);
    }
}
