package androidx.test.internal.runner.junit3;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.Ignore;

/* access modifiers changed from: package-private */
@Ignore
public class DelegatingTestSuite extends TestSuite {
    private TestSuite wrappedSuite;

    public DelegatingTestSuite(TestSuite suiteDelegate) {
        this.wrappedSuite = suiteDelegate;
    }

    public TestSuite getDelegateSuite() {
        return this.wrappedSuite;
    }

    public void setDelegateSuite(TestSuite newSuiteDelegate) {
        this.wrappedSuite = newSuiteDelegate;
    }

    @Override // junit.framework.TestSuite
    public void addTest(Test test) {
        this.wrappedSuite.addTest(test);
    }

    @Override // junit.framework.TestSuite, junit.framework.Test
    public int countTestCases() {
        return this.wrappedSuite.countTestCases();
    }

    @Override // junit.framework.TestSuite
    public String getName() {
        return this.wrappedSuite.getName();
    }

    @Override // junit.framework.TestSuite
    public void runTest(Test test, TestResult result) {
        this.wrappedSuite.runTest(test, result);
    }

    @Override // junit.framework.TestSuite
    public void setName(String name) {
        this.wrappedSuite.setName(name);
    }

    @Override // junit.framework.TestSuite
    public Test testAt(int index) {
        return this.wrappedSuite.testAt(index);
    }

    @Override // junit.framework.TestSuite
    public int testCount() {
        return this.wrappedSuite.testCount();
    }

    @Override // junit.framework.TestSuite
    public String toString() {
        return this.wrappedSuite.toString();
    }

    @Override // junit.framework.TestSuite, junit.framework.Test
    public void run(TestResult result) {
        this.wrappedSuite.run(result);
    }
}
