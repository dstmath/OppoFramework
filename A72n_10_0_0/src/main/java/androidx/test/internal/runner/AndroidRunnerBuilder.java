package androidx.test.internal.runner;

import androidx.test.internal.runner.junit3.AndroidJUnit3Builder;
import androidx.test.internal.runner.junit3.AndroidSuiteBuilder;
import androidx.test.internal.runner.junit4.AndroidAnnotatedBuilder;
import androidx.test.internal.runner.junit4.AndroidJUnit4Builder;
import androidx.test.internal.util.AndroidRunnerParams;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.internal.builders.IgnoredBuilder;
import org.junit.internal.builders.JUnit3Builder;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

class AndroidRunnerBuilder extends AllDefaultPossibilitiesBuilder {
    private final AndroidAnnotatedBuilder androidAnnotatedBuilder;
    private final AndroidJUnit3Builder androidJUnit3Builder;
    private final AndroidJUnit4Builder androidJUnit4Builder;
    private final AndroidSuiteBuilder androidSuiteBuilder;
    private final List<RunnerBuilder> customRunnerBuilders;
    private final IgnoredBuilder ignoredBuilder;

    AndroidRunnerBuilder(AndroidRunnerParams runnerParams, boolean scanningPath, List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses) {
        this(null, runnerParams, scanningPath, customRunnerBuilderClasses);
    }

    AndroidRunnerBuilder(RunnerBuilder suiteBuilder, AndroidRunnerParams runnerParams, boolean scanningPath, List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses) {
        super(true);
        this.androidJUnit3Builder = new AndroidJUnit3Builder(runnerParams, scanningPath);
        this.androidJUnit4Builder = new AndroidJUnit4Builder(runnerParams, scanningPath);
        this.androidSuiteBuilder = new AndroidSuiteBuilder(runnerParams);
        this.androidAnnotatedBuilder = new AndroidAnnotatedBuilder(suiteBuilder == null ? this : suiteBuilder, runnerParams);
        this.ignoredBuilder = new IgnoredBuilder();
        this.customRunnerBuilders = instantiateRunnerBuilders(customRunnerBuilderClasses);
    }

    private List<RunnerBuilder> instantiateRunnerBuilders(List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses) {
        List<RunnerBuilder> runnerBuilders = new ArrayList<>();
        for (Class<? extends RunnerBuilder> customRunnerBuilderClass : customRunnerBuilderClasses) {
            try {
                runnerBuilders.add((RunnerBuilder) customRunnerBuilderClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
            } catch (InstantiationException e) {
                String valueOf = String.valueOf(customRunnerBuilderClass);
                StringBuilder sb = new StringBuilder(113 + String.valueOf(valueOf).length());
                sb.append("Could not create instance of ");
                sb.append(valueOf);
                sb.append(", make sure that it is a public concrete class with a public no-argument constructor");
                throw new IllegalStateException(sb.toString(), e);
            } catch (IllegalAccessException e2) {
                String valueOf2 = String.valueOf(customRunnerBuilderClass);
                StringBuilder sb2 = new StringBuilder(113 + String.valueOf(valueOf2).length());
                sb2.append("Could not create instance of ");
                sb2.append(valueOf2);
                sb2.append(", make sure that it is a public concrete class with a public no-argument constructor");
                throw new IllegalStateException(sb2.toString(), e2);
            } catch (NoSuchMethodException e3) {
                String valueOf3 = String.valueOf(customRunnerBuilderClass);
                StringBuilder sb3 = new StringBuilder(113 + String.valueOf(valueOf3).length());
                sb3.append("Could not create instance of ");
                sb3.append(valueOf3);
                sb3.append(", make sure that it is a public concrete class with a public no-argument constructor");
                throw new IllegalStateException(sb3.toString(), e3);
            } catch (InvocationTargetException e4) {
                String valueOf4 = String.valueOf(customRunnerBuilderClass);
                StringBuilder sb4 = new StringBuilder(74 + String.valueOf(valueOf4).length());
                sb4.append("Could not create instance of ");
                sb4.append(valueOf4);
                sb4.append(", the constructor must not throw an exception");
                throw new IllegalStateException(sb4.toString(), e4);
            }
        }
        return runnerBuilders;
    }

    @Override // org.junit.runners.model.RunnerBuilder, org.junit.internal.builders.AllDefaultPossibilitiesBuilder
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        for (RunnerBuilder customRunnerBuilder : this.customRunnerBuilders) {
            Runner runner = customRunnerBuilder.safeRunnerForClass(testClass);
            if (runner != null) {
                return runner;
            }
        }
        return super.runnerForClass(testClass);
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.internal.builders.AllDefaultPossibilitiesBuilder
    public JUnit4Builder junit4Builder() {
        return this.androidJUnit4Builder;
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.internal.builders.AllDefaultPossibilitiesBuilder
    public JUnit3Builder junit3Builder() {
        return this.androidJUnit3Builder;
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.internal.builders.AllDefaultPossibilitiesBuilder
    public AnnotatedBuilder annotatedBuilder() {
        return this.androidAnnotatedBuilder;
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.internal.builders.AllDefaultPossibilitiesBuilder
    public IgnoredBuilder ignoredBuilder() {
        return this.ignoredBuilder;
    }

    /* access modifiers changed from: protected */
    @Override // org.junit.internal.builders.AllDefaultPossibilitiesBuilder
    public RunnerBuilder suiteMethodBuilder() {
        return this.androidSuiteBuilder;
    }
}
