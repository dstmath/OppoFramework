package androidx.test.internal.runner.junit3;

import java.lang.annotation.Annotation;
import junit.extensions.TestDecorator;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class JUnit38ClassRunner extends Runner implements Filterable {
    private volatile Test fTest;

    /* access modifiers changed from: private */
    public static final class OldTestClassAdaptingListener implements TestListener {
        private Test currentTest;
        private Description description;
        private final RunNotifier fNotifier;

        private OldTestClassAdaptingListener(RunNotifier notifier) {
            this.currentTest = null;
            this.description = null;
            this.fNotifier = notifier;
        }

        @Override // junit.framework.TestListener
        public void endTest(Test test) {
            this.fNotifier.fireTestFinished(asDescription(test));
        }

        @Override // junit.framework.TestListener
        public void startTest(Test test) {
            this.fNotifier.fireTestStarted(asDescription(test));
        }

        @Override // junit.framework.TestListener
        public void addError(Test test, Throwable t) {
            this.fNotifier.fireTestFailure(new Failure(asDescription(test), t));
        }

        private Description asDescription(Test test) {
            if (this.currentTest != null && this.currentTest.equals(test) && this.description != null) {
                return this.description;
            }
            this.currentTest = test;
            if (test instanceof Describable) {
                this.description = ((Describable) test).getDescription();
            } else if (test instanceof TestCase) {
                this.description = JUnit38ClassRunner.makeDescription(test);
            } else {
                this.description = Description.createTestDescription(getEffectiveClass(test), test.toString());
            }
            return this.description;
        }

        /* JADX DEBUG: Type inference failed for r0v0. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<? extends junit.framework.Test> */
        private Class<? extends Test> getEffectiveClass(Test test) {
            return test.getClass();
        }

        @Override // junit.framework.TestListener
        public void addFailure(Test test, AssertionFailedError t) {
            addError(test, t);
        }
    }

    public JUnit38ClassRunner(Test test) {
        setTest(test);
    }

    @Override // org.junit.runner.Runner
    public void run(RunNotifier notifier) {
        TestResult result = new TestResult();
        result.addListener(createAdaptingListener(notifier));
        getTest().run(result);
    }

    public TestListener createAdaptingListener(RunNotifier notifier) {
        return new OldTestClassAdaptingListener(notifier);
    }

    @Override // org.junit.runner.Describable, org.junit.runner.Runner
    public Description getDescription() {
        return makeDescription(getTest());
    }

    static Description makeDescription(Test test) {
        if (test instanceof TestCase) {
            TestCase tc = (TestCase) test;
            return Description.createTestDescription(tc.getClass(), tc.getName(), getAnnotations(tc));
        } else if (test instanceof TestSuite) {
            TestSuite ts = (TestSuite) test;
            Description description = Description.createSuiteDescription(ts.getName() == null ? createSuiteDescription(ts) : ts.getName(), new Annotation[0]);
            int n = ts.testCount();
            for (int i = 0; i < n; i++) {
                description.addChild(makeDescription(ts.testAt(i)));
            }
            return description;
        } else if (test instanceof Describable) {
            return ((Describable) test).getDescription();
        } else {
            if (test instanceof TestDecorator) {
                return makeDescription(((TestDecorator) test).getTest());
            }
            return Description.createSuiteDescription(test.getClass());
        }
    }

    private static Annotation[] getAnnotations(TestCase test) {
        try {
            return test.getClass().getMethod(test.getName(), new Class[0]).getDeclaredAnnotations();
        } catch (NoSuchMethodException | SecurityException e) {
            return new Annotation[0];
        }
    }

    private static String createSuiteDescription(TestSuite ts) {
        String example;
        int count = ts.countTestCases();
        if (count == 0) {
            example = "";
        } else {
            example = String.format(" [example: %s]", ts.testAt(0));
        }
        return String.format("TestSuite with %s tests%s", Integer.valueOf(count), example);
    }

    @Override // org.junit.runner.manipulation.Filterable
    public void filter(Filter filter) throws NoTestsRemainException {
        if (getTest() instanceof Filterable) {
            ((Filterable) getTest()).filter(filter);
        } else if (getTest() instanceof TestSuite) {
            TestSuite suite = (TestSuite) getTest();
            TestSuite filtered = new TestSuite(suite.getName());
            int n = suite.testCount();
            for (int i = 0; i < n; i++) {
                Test test = suite.testAt(i);
                if (filter.shouldRun(makeDescription(test))) {
                    filtered.addTest(test);
                }
            }
            setTest(filtered);
            if (filtered.testCount() == 0) {
                throw new NoTestsRemainException();
            }
        }
    }

    private void setTest(Test test) {
        this.fTest = test;
    }

    private Test getTest() {
        return this.fTest;
    }
}
