package androidx.test.internal.runner.listener;

import android.os.Bundle;
import android.util.Log;
import java.io.PrintStream;
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class InstrumentationResultPrinter extends InstrumentationRunListener {
    static final int MAX_TRACE_SIZE = 32768;
    private Description description = Description.EMPTY;
    private final Bundle resultTemplate = new Bundle();
    String testClass = null;
    int testNum = 0;
    Bundle testResult = new Bundle(this.resultTemplate);
    int testResultCode = -999;

    @Override // org.junit.runner.notification.RunListener
    public void testRunStarted(Description description2) throws Exception {
        this.resultTemplate.putString("id", "AndroidJUnitRunner");
        this.resultTemplate.putInt("numtests", description2.testCount());
    }

    @Override // org.junit.runner.notification.RunListener
    public void testStarted(Description description2) throws Exception {
        this.description = description2;
        String testClass2 = description2.getClassName();
        String testName = description2.getMethodName();
        this.testResult = new Bundle(this.resultTemplate);
        this.testResult.putString("class", testClass2);
        this.testResult.putString("test", testName);
        Bundle bundle = this.testResult;
        int i = this.testNum + 1;
        this.testNum = i;
        bundle.putInt("current", i);
        if (testClass2 == null || testClass2.equals(this.testClass)) {
            this.testResult.putString("stream", "");
        } else {
            this.testResult.putString("stream", String.format("\n%s:", testClass2));
            this.testClass = testClass2;
        }
        sendStatus(1, this.testResult);
        this.testResultCode = 0;
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFinished(Description description2) throws Exception {
        if (this.testResultCode == 0) {
            this.testResult.putString("stream", ".");
        }
        sendStatus(this.testResultCode, this.testResult);
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFailure(Failure failure) throws Exception {
        boolean shouldCallFinish = false;
        if (this.description.equals(Description.EMPTY) && this.testNum == 0 && this.testClass == null) {
            testStarted(failure.getDescription());
            shouldCallFinish = true;
        }
        this.testResultCode = -2;
        reportFailure(failure);
        if (shouldCallFinish) {
            testFinished(failure.getDescription());
        }
    }

    @Override // org.junit.runner.notification.RunListener
    public void testAssumptionFailure(Failure failure) {
        this.testResultCode = -4;
        this.testResult.putString("stack", failure.getTrace());
    }

    private void reportFailure(Failure failure) {
        String trace = failure.getTrace();
        if (trace.length() > MAX_TRACE_SIZE) {
            Log.w("InstrumentationResultPrinter", String.format("Stack trace too long, trimmed to first %s characters.", Integer.valueOf((int) MAX_TRACE_SIZE)));
            trace = String.valueOf(trace.substring(0, MAX_TRACE_SIZE)).concat("\n");
        }
        this.testResult.putString("stack", trace);
        this.testResult.putString("stream", String.format("\nError in %s:\n%s", failure.getDescription().getDisplayName(), failure.getTrace()));
    }

    @Override // org.junit.runner.notification.RunListener
    public void testIgnored(Description description2) throws Exception {
        testStarted(description2);
        this.testResultCode = -3;
        testFinished(description2);
    }

    public void reportProcessCrash(Throwable t) {
        try {
            this.testResultCode = -2;
            Failure failure = new Failure(this.description, t);
            this.testResult.putString("stack", failure.getTrace());
            this.testResult.putString("stream", String.format("\nProcess crashed while executing %s:\n%s", this.description.getDisplayName(), failure.getTrace()));
            testFinished(this.description);
        } catch (Exception e) {
            if (this.description == null) {
                Log.e("InstrumentationResultPrinter", "Failed to initialize test before process crash");
                return;
            }
            String displayName = this.description.getDisplayName();
            StringBuilder sb = new StringBuilder(52 + String.valueOf(displayName).length());
            sb.append("Failed to mark test ");
            sb.append(displayName);
            sb.append(" as finished after process crash");
            Log.e("InstrumentationResultPrinter", sb.toString());
        }
    }

    @Override // androidx.test.internal.runner.listener.InstrumentationRunListener
    public void instrumentationRunFinished(PrintStream streamResult, Bundle resultBundle, Result junitResults) {
        new TextListener(streamResult).testRunFinished(junitResults);
    }
}
