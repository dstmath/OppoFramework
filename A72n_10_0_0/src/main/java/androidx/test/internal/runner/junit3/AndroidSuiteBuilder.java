package androidx.test.internal.runner.junit3;

import android.util.Log;
import androidx.test.internal.util.AndroidRunnerParams;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.internal.builders.SuiteMethodBuilder;
import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Runner;

public class AndroidSuiteBuilder extends SuiteMethodBuilder {
    private final AndroidRunnerParams androidRunnerParams;

    public AndroidSuiteBuilder(AndroidRunnerParams runnerParams) {
        this.androidRunnerParams = runnerParams;
    }

    @Override // org.junit.internal.builders.SuiteMethodBuilder, org.junit.runners.model.RunnerBuilder
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        if (this.androidRunnerParams.isIgnoreSuiteMethods()) {
            return null;
        }
        try {
            if (!hasSuiteMethod(testClass)) {
                return null;
            }
            Test t = SuiteMethod.testFromSuiteMethod(testClass);
            if (t instanceof TestSuite) {
                return new JUnit38ClassRunner(new AndroidTestSuite((TestSuite) t, this.androidRunnerParams));
            }
            throw new IllegalArgumentException(String.valueOf(testClass.getName()).concat("#suite() did not return a TestSuite"));
        } catch (Throwable e) {
            Log.e("AndroidSuiteBuilder", "Error constructing runner", e);
            throw e;
        }
    }
}
