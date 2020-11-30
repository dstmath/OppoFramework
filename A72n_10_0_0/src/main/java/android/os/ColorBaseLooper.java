package android.os;

public class ColorBaseLooper {
    protected static ThrowableObserver sThrowableObserver;

    public interface ThrowableObserver {
        boolean shouldCatchThrowable(Throwable th);
    }

    public static ThrowableObserver getThrowableObserver() {
        return sThrowableObserver;
    }

    public static void setThrowableObserver(ThrowableObserver throwableObserver) {
        sThrowableObserver = throwableObserver;
    }
}
