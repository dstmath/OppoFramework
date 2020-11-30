package androidx.test.internal.runner.listener;

import android.util.Log;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class LogRunListener extends RunListener {
    @Override // org.junit.runner.notification.RunListener
    public void testRunStarted(Description description) throws Exception {
        Log.i("TestRunner", String.format("run started: %d tests", Integer.valueOf(description.testCount())));
    }

    @Override // org.junit.runner.notification.RunListener
    public void testRunFinished(Result result) throws Exception {
        Log.i("TestRunner", String.format("run finished: %d tests, %d failed, %d ignored", Integer.valueOf(result.getRunCount()), Integer.valueOf(result.getFailureCount()), Integer.valueOf(result.getIgnoreCount())));
    }

    @Override // org.junit.runner.notification.RunListener
    public void testStarted(Description description) throws Exception {
        String valueOf = String.valueOf(description.getDisplayName());
        Log.i("TestRunner", valueOf.length() != 0 ? "started: ".concat(valueOf) : new String("started: "));
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFinished(Description description) throws Exception {
        String valueOf = String.valueOf(description.getDisplayName());
        Log.i("TestRunner", valueOf.length() != 0 ? "finished: ".concat(valueOf) : new String("finished: "));
    }

    @Override // org.junit.runner.notification.RunListener
    public void testFailure(Failure failure) throws Exception {
        String valueOf = String.valueOf(failure.getDescription().getDisplayName());
        Log.e("TestRunner", valueOf.length() != 0 ? "failed: ".concat(valueOf) : new String("failed: "));
        Log.e("TestRunner", "----- begin exception -----");
        Log.e("TestRunner", failure.getTrace());
        Log.e("TestRunner", "----- end exception -----");
    }

    @Override // org.junit.runner.notification.RunListener
    public void testAssumptionFailure(Failure failure) {
        String valueOf = String.valueOf(failure.getDescription().getDisplayName());
        Log.e("TestRunner", valueOf.length() != 0 ? "assumption failed: ".concat(valueOf) : new String("assumption failed: "));
        Log.e("TestRunner", "----- begin exception -----");
        Log.e("TestRunner", failure.getTrace());
        Log.e("TestRunner", "----- end exception -----");
    }

    @Override // org.junit.runner.notification.RunListener
    public void testIgnored(Description description) throws Exception {
        String valueOf = String.valueOf(description.getDisplayName());
        Log.i("TestRunner", valueOf.length() != 0 ? "ignored: ".concat(valueOf) : new String("ignored: "));
    }
}
