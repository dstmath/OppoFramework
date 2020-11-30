package androidx.test.internal.runner.junit3;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.Ignore;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;

@Ignore
public class NonExecutingTestSuite extends DelegatingFilterableTestSuite {
    @Override // junit.framework.TestSuite, androidx.test.internal.runner.junit3.DelegatingTestSuite
    public /* bridge */ /* synthetic */ void addTest(Test test) {
        super.addTest(test);
    }

    @Override // junit.framework.TestSuite, junit.framework.Test, androidx.test.internal.runner.junit3.DelegatingTestSuite
    public /* bridge */ /* synthetic */ int countTestCases() {
        return super.countTestCases();
    }

    @Override // org.junit.runner.manipulation.Filterable, androidx.test.internal.runner.junit3.DelegatingFilterableTestSuite
    public /* bridge */ /* synthetic */ void filter(Filter filter) throws NoTestsRemainException {
        super.filter(filter);
    }

    @Override // androidx.test.internal.runner.junit3.DelegatingTestSuite
    public /* bridge */ /* synthetic */ TestSuite getDelegateSuite() {
        return super.getDelegateSuite();
    }

    @Override // junit.framework.TestSuite, androidx.test.internal.runner.junit3.DelegatingTestSuite
    public /* bridge */ /* synthetic */ String getName() {
        return super.getName();
    }

    @Override // junit.framework.TestSuite, androidx.test.internal.runner.junit3.DelegatingTestSuite
    public /* bridge */ /* synthetic */ void runTest(Test test, TestResult testResult) {
        super.runTest(test, testResult);
    }

    @Override // androidx.test.internal.runner.junit3.DelegatingTestSuite
    public /* bridge */ /* synthetic */ void setDelegateSuite(TestSuite testSuite) {
        super.setDelegateSuite(testSuite);
    }

    @Override // junit.framework.TestSuite, androidx.test.internal.runner.junit3.DelegatingTestSuite
    public /* bridge */ /* synthetic */ void setName(String str) {
        super.setName(str);
    }

    @Override // junit.framework.TestSuite, androidx.test.internal.runner.junit3.DelegatingTestSuite
    public /* bridge */ /* synthetic */ Test testAt(int i) {
        return super.testAt(i);
    }

    @Override // junit.framework.TestSuite, androidx.test.internal.runner.junit3.DelegatingTestSuite
    public /* bridge */ /* synthetic */ int testCount() {
        return super.testCount();
    }

    @Override // junit.framework.TestSuite, androidx.test.internal.runner.junit3.DelegatingTestSuite
    public /* bridge */ /* synthetic */ String toString() {
        return super.toString();
    }

    public NonExecutingTestSuite(Class<?> testClass) {
        this(new TestSuite(testClass));
    }

    public NonExecutingTestSuite(TestSuite s) {
        super(s);
    }

    @Override // junit.framework.TestSuite, junit.framework.Test, androidx.test.internal.runner.junit3.DelegatingTestSuite
    public void run(TestResult result) {
        super.run(new NonExecutingTestResult(result));
    }
}
