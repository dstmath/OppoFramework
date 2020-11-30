package androidx.test.internal.runner.junit4;

import android.util.Log;
import androidx.test.internal.util.AndroidRunnerParams;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;

public class AndroidJUnit4Builder extends JUnit4Builder {
    private final AndroidRunnerParams androidRunnerParams;
    private final boolean scanningPath;

    public AndroidJUnit4Builder(AndroidRunnerParams runnerParams, boolean scanningPath2) {
        this.androidRunnerParams = runnerParams;
        this.scanningPath = scanningPath2;
    }

    @Override // org.junit.internal.builders.JUnit4Builder, org.junit.runners.model.RunnerBuilder
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        try {
            if (!this.scanningPath || hasTestMethods(testClass)) {
                return new AndroidJUnit4ClassRunner(testClass, this.androidRunnerParams);
            }
            return null;
        } catch (Throwable e) {
            Log.e("AndroidJUnit4Builder", "Error constructing runner", e);
            throw e;
        }
    }

    private static boolean hasTestMethods(Class<?> testClass) {
        try {
            for (Method testMethod : testClass.getMethods()) {
                if (testMethod.isAnnotationPresent(Test.class)) {
                    return true;
                }
            }
            return false;
        } catch (Throwable t) {
            Log.w("AndroidJUnit4Builder", String.format("%s in hasTestMethods for %s", t.toString(), testClass.getName()));
            return false;
        }
    }
}
