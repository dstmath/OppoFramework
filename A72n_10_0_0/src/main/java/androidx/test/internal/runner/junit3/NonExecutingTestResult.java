package androidx.test.internal.runner.junit3;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;

class NonExecutingTestResult extends DelegatingTestResult {
    NonExecutingTestResult(TestResult wrappedResult) {
        super(wrappedResult);
    }

    /* access modifiers changed from: protected */
    @Override // junit.framework.TestResult
    public void run(TestCase test) {
        startTest(test);
        endTest(test);
    }

    @Override // junit.framework.TestResult, androidx.test.internal.runner.junit3.DelegatingTestResult
    public void runProtected(Test test, Protectable p) {
    }
}
