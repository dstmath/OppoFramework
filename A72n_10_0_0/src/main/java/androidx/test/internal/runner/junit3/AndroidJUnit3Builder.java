package androidx.test.internal.runner.junit3;

import android.util.Log;
import androidx.test.internal.util.AndroidRunnerBuilderUtil;
import androidx.test.internal.util.AndroidRunnerParams;
import org.junit.internal.builders.JUnit3Builder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class AndroidJUnit3Builder extends JUnit3Builder {
    public static final Runner NOT_A_VALID_TEST = new Runner() {
        /* class androidx.test.internal.runner.junit3.AndroidJUnit3Builder.AnonymousClass1 */

        @Override // org.junit.runner.Describable, org.junit.runner.Runner
        public Description getDescription() {
            return Description.EMPTY;
        }

        @Override // org.junit.runner.Runner
        public void run(RunNotifier notifier) {
        }
    };
    private final AndroidRunnerParams androidRunnerParams;
    private final boolean scanningPath;

    public AndroidJUnit3Builder(AndroidRunnerParams runnerParams, boolean scanningPath2) {
        this.androidRunnerParams = runnerParams;
        this.scanningPath = scanningPath2;
    }

    @Override // org.junit.internal.builders.JUnit3Builder, org.junit.runners.model.RunnerBuilder
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        try {
            if (!AndroidRunnerBuilderUtil.isJUnit3Test(testClass)) {
                return null;
            }
            if (!this.scanningPath || AndroidRunnerBuilderUtil.hasJUnit3TestMethod(testClass)) {
                return new JUnit38ClassRunner(new AndroidTestSuite(testClass, this.androidRunnerParams));
            }
            return NOT_A_VALID_TEST;
        } catch (Throwable e) {
            Log.e("AndroidJUnit3Builder", "Error constructing runner", e);
            throw e;
        }
    }
}
