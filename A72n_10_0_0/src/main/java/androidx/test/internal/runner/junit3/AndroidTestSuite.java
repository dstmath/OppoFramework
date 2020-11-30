package androidx.test.internal.runner.junit3;

import android.os.Looper;
import android.util.Log;
import androidx.test.internal.util.AndroidRunnerParams;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.Ignore;

@Ignore
class AndroidTestSuite extends DelegatingFilterableTestSuite {
    private final AndroidRunnerParams androidRunnerParams;

    public AndroidTestSuite(Class<?> testClass, AndroidRunnerParams runnerParams) {
        this(new NonLeakyTestSuite(testClass), runnerParams);
    }

    public AndroidTestSuite(TestSuite s, AndroidRunnerParams runnerParams) {
        super(s);
        this.androidRunnerParams = runnerParams;
    }

    @Override // junit.framework.TestSuite, junit.framework.Test, androidx.test.internal.runner.junit3.DelegatingTestSuite
    public void run(TestResult result) {
        AndroidTestResult androidTestResult = new AndroidTestResult(this.androidRunnerParams.getBundle(), this.androidRunnerParams.getInstrumentation(), result);
        long timeout = this.androidRunnerParams.getPerTestTimeout();
        if (timeout > 0) {
            runTestsWithTimeout(timeout, androidTestResult);
        } else {
            super.run(androidTestResult);
        }
    }

    private void runTestsWithTimeout(long timeout, AndroidTestResult result) {
        int suiteSize = testCount();
        for (int i = 0; i < suiteSize; i++) {
            runTestWithTimeout(testAt(i), result, timeout);
        }
    }

    private void runTestWithTimeout(final Test test, final AndroidTestResult androidTestResult, long timeout) {
        ExecutorService threadExecutor = Executors.newSingleThreadExecutor(new ThreadFactory(this) {
            /* class androidx.test.internal.runner.junit3.AndroidTestSuite.AnonymousClass1 */

            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setName("AndroidTestSuite");
                return t;
            }
        });
        Runnable execRunnable = new Runnable(this) {
            /* class androidx.test.internal.runner.junit3.AndroidTestSuite.AnonymousClass2 */

            public void run() {
                test.run(androidTestResult);
            }
        };
        androidTestResult.setCurrentTimeout(timeout);
        Future<?> result = threadExecutor.submit(execRunnable);
        threadExecutor.shutdown();
        try {
            if (!threadExecutor.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                threadExecutor.shutdownNow();
                if (!threadExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
                    Log.e("AndroidTestSuite", "Failed to to stop test execution thread, the correctness of the test runner is at risk. Abort all execution!");
                    try {
                        result.get(0, TimeUnit.MILLISECONDS);
                    } catch (ExecutionException e) {
                        Log.e("AndroidTestSuite", "Exception from the execution thread", e.getCause());
                    } catch (TimeoutException e2) {
                        Log.e("AndroidTestSuite", "Exception from the execution thread", e2);
                    }
                    terminateAllRunnerExecution(new IllegalStateException(String.format("Test timed out after %d milliseconds but execution thread failed to terminate\nDumping instr and main threads:\n%s", Long.valueOf(timeout), getStackTraces())));
                }
            }
        } catch (InterruptedException e3) {
            Log.e("AndroidTestSuite", "The correctness of the test runner is at risk. Abort all execution!");
            terminateAllRunnerExecution(new IllegalStateException(String.format("Test execution thread got interrupted:\n%s\nDumping instr and main threads:\n%s", e3, getStackTraces())));
        }
    }

    private void terminateAllRunnerExecution(final RuntimeException exception) {
        Thread t = new Thread(new Runnable(this) {
            /* class androidx.test.internal.runner.junit3.AndroidTestSuite.AnonymousClass3 */

            public void run() {
                throw exception;
            }
        }, "Terminator");
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
        }
    }

    private String getStackTraces() {
        StringBuilder sb = new StringBuilder();
        Thread t = Thread.currentThread();
        sb.append(t.toString());
        sb.append('\n');
        StackTraceElement[] stackTrace = t.getStackTrace();
        for (StackTraceElement ste : stackTrace) {
            sb.append("\tat ");
            sb.append(ste.toString());
            sb.append('\n');
        }
        sb.append('\n');
        Thread t2 = Looper.getMainLooper().getThread();
        sb.append(t2.toString());
        sb.append('\n');
        StackTraceElement[] stackTrace2 = t2.getStackTrace();
        for (StackTraceElement ste2 : stackTrace2) {
            sb.append("\tat ");
            sb.append(ste2.toString());
            sb.append('\n');
        }
        sb.append('\n');
        return sb.toString();
    }
}
