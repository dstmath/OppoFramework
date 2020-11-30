package androidx.test.internal.runner.junit3;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.Ignore;
import org.junit.runner.Describable;
import org.junit.runner.Description;

@Ignore
public class NonLeakyTestSuite extends TestSuite {
    public NonLeakyTestSuite(Class<?> testClass) {
        super(testClass);
    }

    @Override // junit.framework.TestSuite
    public void addTest(Test test) {
        super.addTest(new NonLeakyTest(test));
    }

    private static class NonLeakyTest implements Test, Describable {
        private Test delegate;
        private final Description desc = JUnit38ClassRunner.makeDescription(this.delegate);

        NonLeakyTest(Test delegate2) {
            this.delegate = delegate2;
        }

        @Override // junit.framework.Test
        public int countTestCases() {
            if (this.delegate != null) {
                return this.delegate.countTestCases();
            }
            return 0;
        }

        @Override // junit.framework.Test
        public void run(TestResult result) {
            this.delegate.run(result);
            this.delegate = null;
        }

        @Override // org.junit.runner.Describable
        public Description getDescription() {
            return this.desc;
        }

        public String toString() {
            if (this.delegate != null) {
                return this.delegate.toString();
            }
            return this.desc.toString();
        }
    }
}
