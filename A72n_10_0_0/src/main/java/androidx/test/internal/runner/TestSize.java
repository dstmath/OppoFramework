package androidx.test.internal.runner;

import androidx.test.filters.LargeTest;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SmallTest;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.runner.Description;

public final class TestSize {
    private static final Set<TestSize> ALL_SIZES = Collections.unmodifiableSet(new HashSet(Arrays.asList(SMALL, MEDIUM, LARGE)));
    public static final TestSize LARGE = new TestSize("large", LargeTest.class, android.test.suitebuilder.annotation.LargeTest.class, Float.MAX_VALUE);
    public static final TestSize MEDIUM = new TestSize("medium", MediumTest.class, android.test.suitebuilder.annotation.MediumTest.class, 1000.0f);
    public static final TestSize NONE = new TestSize("", null, null, 0.0f);
    public static final TestSize SMALL = new TestSize("small", SmallTest.class, android.test.suitebuilder.annotation.SmallTest.class, 200.0f);
    private final Class<? extends Annotation> platformAnnotationClass;
    private final Class<? extends Annotation> runnerFilterAnnotationClass;
    private final String sizeQualifierName;
    private final float testSizeRunTimeThreshold;

    public TestSize(String sizeQualifierName2, Class<? extends Annotation> platformAnnotationClass2, Class<? extends Annotation> runnerFilterAnnotationClass2, float testSizeRuntimeThreshold) {
        this.sizeQualifierName = sizeQualifierName2;
        this.platformAnnotationClass = platformAnnotationClass2;
        this.runnerFilterAnnotationClass = runnerFilterAnnotationClass2;
        this.testSizeRunTimeThreshold = testSizeRuntimeThreshold;
    }

    public String getSizeQualifierName() {
        return this.sizeQualifierName;
    }

    public boolean testMethodIsAnnotatedWithTestSize(Description description) {
        if (description.getAnnotation(this.runnerFilterAnnotationClass) == null && description.getAnnotation(this.platformAnnotationClass) == null) {
            return false;
        }
        return true;
    }

    public boolean testClassIsAnnotatedWithTestSize(Description description) {
        Class<?> testClass = description.getTestClass();
        if (testClass == null) {
            return false;
        }
        if (testClass.isAnnotationPresent(this.runnerFilterAnnotationClass) || testClass.isAnnotationPresent(this.platformAnnotationClass)) {
            return true;
        }
        return false;
    }

    public float getRunTimeThreshold() {
        return this.testSizeRunTimeThreshold;
    }

    public static TestSize getTestSizeForRunTime(float testRuntime) {
        if (runTimeSmallerThanThreshold(testRuntime, SMALL.getRunTimeThreshold())) {
            return SMALL;
        }
        if (runTimeSmallerThanThreshold(testRuntime, MEDIUM.getRunTimeThreshold())) {
            return MEDIUM;
        }
        return LARGE;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x000c  */
    public static boolean isAnyTestSize(Class<? extends Annotation> annotationClass) {
        for (TestSize testSize : ALL_SIZES) {
            if (testSize.getRunnerAnnotation() == annotationClass || testSize.getFrameworkAnnotation() == annotationClass) {
                return true;
            }
            while (r0.hasNext()) {
            }
        }
        return false;
    }

    public static TestSize fromString(String testSize) {
        TestSize testSizeFromString = NONE;
        for (TestSize testSizeValue : ALL_SIZES) {
            if (testSizeValue.getSizeQualifierName().equals(testSize)) {
                testSizeFromString = testSizeValue;
            }
        }
        return testSizeFromString;
    }

    public static TestSize fromDescription(Description description) {
        TestSize testSize = NONE;
        Iterator<TestSize> it = ALL_SIZES.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            TestSize testMethodSizeValue = it.next();
            if (testMethodSizeValue.testMethodIsAnnotatedWithTestSize(description)) {
                testSize = testMethodSizeValue;
                break;
            }
        }
        if (!NONE.equals(testSize)) {
            return testSize;
        }
        for (TestSize testClassSizeValue : ALL_SIZES) {
            if (testClassSizeValue.testClassIsAnnotatedWithTestSize(description)) {
                return testClassSizeValue;
            }
        }
        return testSize;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.sizeQualifierName.equals(((TestSize) o).sizeQualifierName);
    }

    public int hashCode() {
        return this.sizeQualifierName.hashCode();
    }

    private static boolean runTimeSmallerThanThreshold(float testRuntime, float runtimeThreshold) {
        return Float.compare(testRuntime, runtimeThreshold) < 0;
    }

    private Class<? extends Annotation> getFrameworkAnnotation() {
        return this.platformAnnotationClass;
    }

    private Class<? extends Annotation> getRunnerAnnotation() {
        return this.runnerFilterAnnotationClass;
    }
}
