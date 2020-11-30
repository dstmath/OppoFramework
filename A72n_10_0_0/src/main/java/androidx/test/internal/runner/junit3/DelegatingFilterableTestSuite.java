package androidx.test.internal.runner.junit3;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;

/* access modifiers changed from: package-private */
@Ignore
public class DelegatingFilterableTestSuite extends DelegatingTestSuite implements Filterable {
    public DelegatingFilterableTestSuite(TestSuite suiteDelegate) {
        super(suiteDelegate);
    }

    @Override // org.junit.runner.manipulation.Filterable
    public void filter(Filter filter) throws NoTestsRemainException {
        TestSuite suite = getDelegateSuite();
        TestSuite filtered = new TestSuite(suite.getName());
        int n = suite.testCount();
        for (int i = 0; i < n; i++) {
            Test test = suite.testAt(i);
            if (filter.shouldRun(makeDescription(test))) {
                filtered.addTest(test);
            }
        }
        setDelegateSuite(filtered);
        if (filtered.testCount() == 0) {
            throw new NoTestsRemainException();
        }
    }

    private static Description makeDescription(Test test) {
        return JUnit38ClassRunner.makeDescription(test);
    }
}
