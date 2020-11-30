package androidx.test.internal.runner.junit4.statement;

import android.os.Looper;
import android.test.UiThreadTest;
import android.util.Log;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class UiThreadStatement extends Statement {
    private final Statement base;
    private final boolean runOnUiThread;

    public UiThreadStatement(Statement base2, boolean runOnUiThread2) {
        this.base = base2;
        this.runOnUiThread = runOnUiThread2;
    }

    @Override // org.junit.runners.model.Statement
    public void evaluate() throws Throwable {
        if (this.runOnUiThread) {
            final AtomicReference<Throwable> exceptionRef = new AtomicReference<>();
            runOnUiThread(new Runnable() {
                /* class androidx.test.internal.runner.junit4.statement.UiThreadStatement.AnonymousClass1 */

                public void run() {
                    try {
                        UiThreadStatement.this.base.evaluate();
                    } catch (Throwable throwable) {
                        exceptionRef.set(throwable);
                    }
                }
            });
            Throwable throwable = exceptionRef.get();
            if (throwable != null) {
                throw throwable;
            }
            return;
        }
        this.base.evaluate();
    }

    public static boolean shouldRunOnUiThread(FrameworkMethod method) {
        if (method.getAnnotation(UiThreadTest.class) != null) {
            return true;
        }
        try {
            Class UiThreadTestClass = Class.forName("androidx.test.annotation.UiThreadTest");
            if (method.getAnnotation(UiThreadTest.class) == null && method.getAnnotation(UiThreadTestClass) == null) {
                return false;
            }
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void runOnUiThread(Runnable runnable) throws Throwable {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w("UiThreadStatement", "Already on the UI thread, this method should not be called from the main application thread");
            runnable.run();
            return;
        }
        FutureTask<Void> task = new FutureTask<>(runnable, null);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(task);
        try {
            task.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }
}
