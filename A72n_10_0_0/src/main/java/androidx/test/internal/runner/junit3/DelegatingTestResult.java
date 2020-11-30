package androidx.test.internal.runner.junit3;

import junit.framework.AssertionFailedError;
import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;

class DelegatingTestResult extends TestResult {
    private TestResult wrappedResult;

    DelegatingTestResult(TestResult wrappedResult2) {
        this.wrappedResult = wrappedResult2;
    }

    @Override // junit.framework.TestResult
    public void addError(Test test, Throwable t) {
        this.wrappedResult.addError(test, t);
    }

    @Override // junit.framework.TestResult
    public void addFailure(Test test, AssertionFailedError t) {
        this.wrappedResult.addFailure(test, t);
    }

    @Override // junit.framework.TestResult
    public void addListener(TestListener listener) {
        this.wrappedResult.addListener(listener);
    }

    @Override // junit.framework.TestResult
    public void endTest(Test test) {
        this.wrappedResult.endTest(test);
    }

    @Override // junit.framework.TestResult
    public void runProtected(Test test, Protectable p) {
        this.wrappedResult.runProtected(test, p);
    }

    @Override // junit.framework.TestResult
    public boolean shouldStop() {
        return this.wrappedResult.shouldStop();
    }

    @Override // junit.framework.TestResult
    public void startTest(Test test) {
        this.wrappedResult.startTest(test);
    }
}
