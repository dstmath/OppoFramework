package androidx.test.internal.runner.listener;

import android.util.Log;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

public class DelayInjector extends RunListener {
    private final int delayMsec;

    public DelayInjector(int delayMsec2) {
        this.delayMsec = delayMsec2;
    }

    @Override // org.junit.runner.notification.RunListener
    public void testRunStarted(Description description) throws Exception {
        delay();
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFinished(Description description) throws Exception {
        delay();
    }

    private void delay() {
        try {
            Thread.sleep((long) this.delayMsec);
        } catch (InterruptedException e) {
            Log.e("DelayInjector", "interrupted", e);
        }
    }
}
