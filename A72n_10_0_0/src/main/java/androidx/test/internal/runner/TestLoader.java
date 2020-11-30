package androidx.test.internal.runner;

import android.util.Log;
import androidx.test.internal.runner.junit3.AndroidJUnit3Builder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.RunnerBuilder;

class TestLoader {
    private final ClassLoader classLoader;
    private final RunnerBuilder runnerBuilder;
    private final Map<String, Runner> runnersMap = new LinkedHashMap();

    static TestLoader testLoader(ClassLoader classLoader2, RunnerBuilder runnerBuilder2, boolean scanningPath) {
        if (scanningPath) {
            runnerBuilder2 = new ScanningRunnerBuilder(runnerBuilder2);
        }
        if (classLoader2 == null) {
            classLoader2 = TestLoader.class.getClassLoader();
        }
        return new TestLoader(classLoader2, runnerBuilder2);
    }

    private TestLoader(ClassLoader classLoader2, RunnerBuilder runnerBuilder2) {
        this.classLoader = classLoader2;
        this.runnerBuilder = runnerBuilder2;
    }

    private void doCreateRunner(String className, boolean isScanningPath) {
        Runner runner;
        if (!this.runnersMap.containsKey(className)) {
            try {
                Class<?> loadedClass = Class.forName(className, false, this.classLoader);
                runner = this.runnerBuilder.safeRunnerForClass(loadedClass);
                if (runner == null) {
                    logDebug(String.format("Skipping class %s: not a test", loadedClass.getName()));
                } else if (runner == AndroidJUnit3Builder.NOT_A_VALID_TEST) {
                    logDebug(String.format("Skipping class %s: not a valid test", loadedClass.getName()));
                    runner = null;
                }
            } catch (ClassNotFoundException e) {
                Log.e("TestLoader", String.format("Could not find class: %s", className));
                Description description = Description.createSuiteDescription(className, new Annotation[0]);
                runner = !isScanningPath ? new UnloadableClassRunner(description, new Failure(description, e)) : null;
            }
            if (runner != null) {
                this.runnersMap.put(className, runner);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public List<Runner> getRunnersFor(Collection<String> classNames, boolean isScanningPath) {
        for (String className : classNames) {
            doCreateRunner(className, isScanningPath);
        }
        return new ArrayList(this.runnersMap.values());
    }

    /* access modifiers changed from: private */
    public static void logDebug(String msg) {
        if (Log.isLoggable("TestLoader", 3)) {
            Log.d("TestLoader", msg);
        }
    }

    private static class ScanningRunnerBuilder extends RunnerBuilder {
        private final RunnerBuilder runnerBuilder;

        ScanningRunnerBuilder(RunnerBuilder runnerBuilder2) {
            this.runnerBuilder = runnerBuilder2;
        }

        @Override // org.junit.runners.model.RunnerBuilder
        public Runner runnerForClass(Class<?> testClass) throws Throwable {
            if (!Modifier.isAbstract(testClass.getModifiers())) {
                return this.runnerBuilder.runnerForClass(testClass);
            }
            TestLoader.logDebug(String.format("Skipping abstract class %s: not a test", testClass.getName()));
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public static class UnloadableClassRunner extends Runner {
        private final Description description;
        private final Failure failure;

        UnloadableClassRunner(Description description2, Failure failure2) {
            this.description = description2;
            this.failure = failure2;
        }

        @Override // org.junit.runner.Describable, org.junit.runner.Runner
        public Description getDescription() {
            return this.description;
        }

        @Override // org.junit.runner.Runner
        public void run(RunNotifier notifier) {
            notifier.fireTestStarted(this.description);
            notifier.fireTestFailure(this.failure);
            notifier.fireTestFinished(this.description);
        }
    }
}
