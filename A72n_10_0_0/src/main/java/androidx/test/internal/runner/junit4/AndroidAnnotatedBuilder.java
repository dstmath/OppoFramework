package androidx.test.internal.runner.junit4;

import android.util.Log;
import androidx.test.internal.util.AndroidRunnerParams;
import androidx.test.runner.AndroidJUnit4;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class AndroidAnnotatedBuilder extends AnnotatedBuilder {
    private final AndroidRunnerParams androidRunnerParams;

    public AndroidAnnotatedBuilder(RunnerBuilder suiteBuilder, AndroidRunnerParams runnerParams) {
        super(suiteBuilder);
        this.androidRunnerParams = runnerParams;
    }

    @Override // org.junit.runners.model.RunnerBuilder, org.junit.internal.builders.AnnotatedBuilder
    public Runner runnerForClass(Class<?> testClass) throws Exception {
        try {
            RunWith annotation = (RunWith) testClass.getAnnotation(RunWith.class);
            if (annotation != null && AndroidJUnit4.class.equals(annotation.value())) {
                Class<? extends Runner> runnerClass = annotation.value();
                try {
                    Runner runner = buildAndroidRunner(runnerClass, testClass);
                    if (runner != null) {
                        return runner;
                    }
                } catch (NoSuchMethodException e) {
                    return super.buildRunner(runnerClass, testClass);
                }
            }
            return super.runnerForClass(testClass);
        } catch (Throwable e2) {
            Log.e("AndroidAnnotatedBuilder", "Error constructing runner", e2);
            throw e2;
        }
    }

    public Runner buildAndroidRunner(Class<? extends Runner> runnerClass, Class<?> testClass) throws Exception {
        return (Runner) runnerClass.getConstructor(Class.class, AndroidRunnerParams.class).newInstance(testClass, this.androidRunnerParams);
    }
}
