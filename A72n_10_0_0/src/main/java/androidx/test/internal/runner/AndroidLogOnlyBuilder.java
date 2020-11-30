package androidx.test.internal.runner;

import androidx.test.internal.runner.junit3.JUnit38ClassRunner;
import androidx.test.internal.runner.junit3.NonExecutingTestSuite;
import androidx.test.internal.util.AndroidRunnerBuilderUtil;
import androidx.test.internal.util.AndroidRunnerParams;
import androidx.test.internal.util.Checks;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

class AndroidLogOnlyBuilder extends RunnerBuilder {
    private final AndroidRunnerBuilder builder;
    private int runnerCount = 0;
    private final AndroidRunnerParams runnerParams;
    private final boolean scanningPath;

    AndroidLogOnlyBuilder(AndroidRunnerParams runnerParams2, boolean scanningPath2, List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses) {
        this.runnerParams = (AndroidRunnerParams) Checks.checkNotNull(runnerParams2, "runnerParams cannot be null!");
        this.scanningPath = scanningPath2;
        this.builder = new AndroidRunnerBuilder(this, runnerParams2, scanningPath2, customRunnerBuilderClasses);
    }

    /* JADX INFO: Multiple debug info for r0v4 int: [D('oldRunnerCount' int), D('test' junit.framework.Test)] */
    @Override // org.junit.runners.model.RunnerBuilder
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        this.runnerCount++;
        if (AndroidRunnerBuilderUtil.isJUnit3Test(testClass)) {
            if (!this.scanningPath || AndroidRunnerBuilderUtil.hasJUnit3TestMethod(testClass)) {
                return new JUnit38ClassRunner(new NonExecutingTestSuite(testClass));
            }
            return null;
        } else if (!AndroidRunnerBuilderUtil.hasSuiteMethod(testClass)) {
            int oldRunnerCount = this.runnerCount;
            Runner runner = this.builder.runnerForClass(testClass);
            if (runner == null) {
                return null;
            }
            if (!(runner instanceof ErrorReportingRunner) && this.runnerCount <= oldRunnerCount) {
                return new NonExecutingRunner(runner);
            }
            return runner;
        } else if (this.runnerParams.isIgnoreSuiteMethods()) {
            return null;
        } else {
            Test test = SuiteMethod.testFromSuiteMethod(testClass);
            if (test instanceof TestSuite) {
                return new JUnit38ClassRunner(new NonExecutingTestSuite((TestSuite) test));
            }
            throw new IllegalArgumentException(String.valueOf(testClass.getName()).concat("#suite() did not return a TestSuite"));
        }
    }
}
