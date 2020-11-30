package androidx.test.internal.runner.junit3;

import android.app.Instrumentation;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import java.util.concurrent.TimeoutException;
import junit.framework.AssertionFailedError;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

/* access modifiers changed from: package-private */
public class AndroidTestResult extends DelegatingTestResult {
    private final Bundle bundle;
    private final Instrumentation instr;
    private long timeout;

    AndroidTestResult(Bundle bundle2, Instrumentation instr2, TestResult result) {
        super(result);
        this.bundle = bundle2;
        this.instr = instr2;
    }

    /* access modifiers changed from: protected */
    @Override // junit.framework.TestResult
    public void run(TestCase test) {
        if (test instanceof AndroidTestCase) {
            ((AndroidTestCase) test).setContext(this.instr.getTargetContext());
        }
        if (test instanceof InstrumentationTestCase) {
            ((InstrumentationTestCase) test).injectInstrumentation(this.instr);
        }
        super.run(test);
    }

    /* access modifiers changed from: package-private */
    public void setCurrentTimeout(long timeout2) {
        this.timeout = timeout2;
    }

    @Override // junit.framework.TestResult, androidx.test.internal.runner.junit3.DelegatingTestResult
    public void runProtected(Test test, Protectable p) {
        try {
            p.protect();
        } catch (AssertionFailedError e) {
            super.addFailure(test, e);
        } catch (ThreadDeath e2) {
            throw e2;
        } catch (InterruptedException e3) {
            super.addError(test, new TimeoutException(String.format("Test timed out after %d milliseconds", Long.valueOf(this.timeout))));
        } catch (Throwable e4) {
            super.addError(test, e4);
        }
    }
}
