package android.support.test.internal.runner;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.filters.RequiresDevice;
import android.support.test.filters.SdkSuppress;
import android.support.test.filters.Suppress;
import android.support.test.internal.util.Checks;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.model.RunnerBuilder;

public class TestRequestBuilder {
    private static final String[] DEFAULT_EXCLUDED_PACKAGES = {"junit", "org.junit", "org.hamcrest", "org.mockito", "android.support.test.internal.runner.junit3", "org.jacoco", "net.bytebuddy"};
    private List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses = new ArrayList();
    private final Bundle mArgsBundle;
    private ClassAndMethodFilter mClassMethodFilter = new ClassAndMethodFilter();
    private final DeviceBuild mDeviceBuild;
    private Set<String> mExcludedClasses = new HashSet();
    private Set<String> mExcludedPackages = new HashSet();
    private Filter mFilter = new AnnotationExclusionFilter(Suppress.class).intersect(new AnnotationExclusionFilter(android.test.suitebuilder.annotation.Suppress.class)).intersect(new SdkSuppressFilter()).intersect(new RequiresDeviceFilter()).intersect(this.mClassMethodFilter);
    private boolean mIgnoreSuiteMethods = false;
    private Set<String> mIncludedClasses = new HashSet();
    private Set<String> mIncludedPackages = new HashSet();
    private final Instrumentation mInstr;
    private long mPerTestTimeout = 0;
    private boolean mSkipExecution = false;
    private final List<String> pathsToScan = new ArrayList();

    /* access modifiers changed from: package-private */
    public interface DeviceBuild {
        String getHardware();

        int getSdkVersionInt();
    }

    private static abstract class ParentFilter extends Filter {
        /* access modifiers changed from: protected */
        public abstract boolean evaluateTest(Description description);

        private ParentFilter() {
        }

        @Override // org.junit.runner.manipulation.Filter
        public boolean shouldRun(Description description) {
            if (description.isTest()) {
                return evaluateTest(description);
            }
            Iterator<Description> it = description.getChildren().iterator();
            while (it.hasNext()) {
                if (shouldRun(it.next())) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class AnnotationExclusionFilter extends ParentFilter {
        private final Class<? extends Annotation> mAnnotationClass;

        AnnotationExclusionFilter(Class<? extends Annotation> annotation) {
            super();
            this.mAnnotationClass = annotation;
        }

        /* access modifiers changed from: protected */
        @Override // android.support.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            Class<?> testClass = description.getTestClass();
            if ((testClass == null || !testClass.isAnnotationPresent(this.mAnnotationClass)) && description.getAnnotation(this.mAnnotationClass) == null) {
                return true;
            }
            return false;
        }
    }

    private class SdkSuppressFilter extends ParentFilter {
        private SdkSuppressFilter() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // android.support.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            SdkSuppress sdkSuppress = getAnnotationForTest(description);
            if (sdkSuppress == null) {
                return true;
            }
            if (TestRequestBuilder.this.getDeviceSdkInt() < sdkSuppress.minSdkVersion() || TestRequestBuilder.this.getDeviceSdkInt() > sdkSuppress.maxSdkVersion()) {
                return false;
            }
            return true;
        }

        private SdkSuppress getAnnotationForTest(Description description) {
            SdkSuppress s = (SdkSuppress) description.getAnnotation(SdkSuppress.class);
            if (s != null) {
                return s;
            }
            Class<?> testClass = description.getTestClass();
            if (testClass != null) {
                return (SdkSuppress) testClass.getAnnotation(SdkSuppress.class);
            }
            return null;
        }
    }

    class RequiresDeviceFilter extends AnnotationExclusionFilter {
        private final Set<String> emulatorHardwareNames = new HashSet(Arrays.asList("goldfish", "ranchu", "gce_x86"));

        RequiresDeviceFilter() {
            super(RequiresDevice.class);
        }

        /* access modifiers changed from: protected */
        @Override // android.support.test.internal.runner.TestRequestBuilder.AnnotationExclusionFilter, android.support.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            if (!super.evaluateTest(description)) {
                return !this.emulatorHardwareNames.contains(TestRequestBuilder.this.getDeviceHardware());
            }
            return true;
        }
    }

    private static class ClassAndMethodFilter extends ParentFilter {
        private Map<String, MethodFilter> mMethodFilters;

        private ClassAndMethodFilter() {
            super();
            this.mMethodFilters = new HashMap();
        }

        @Override // android.support.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            MethodFilter methodFilter;
            if (!this.mMethodFilters.isEmpty() && (methodFilter = this.mMethodFilters.get(description.getClassName())) != null) {
                return methodFilter.shouldRun(description);
            }
            return true;
        }
    }

    private static class MethodFilter extends ParentFilter {
        private Set<String> mExcludedMethods;
        private Set<String> mIncludedMethods;

        @Override // android.support.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            String methodName = description.getMethodName();
            if (methodName == null) {
                return false;
            }
            String methodName2 = stripParameterizedSuffix(methodName);
            if (this.mExcludedMethods.contains(methodName2)) {
                return false;
            }
            if (this.mIncludedMethods.isEmpty() || this.mIncludedMethods.contains(methodName2) || methodName2.equals("initializationError")) {
                return true;
            }
            return false;
        }

        private String stripParameterizedSuffix(String name) {
            if (Pattern.compile(".+(\\[[0-9]+\\])$").matcher(name).matches()) {
                return name.substring(0, name.lastIndexOf(91));
            }
            return name;
        }
    }

    TestRequestBuilder(DeviceBuild deviceBuildAccessor, Instrumentation instr, Bundle bundle) {
        this.mDeviceBuild = (DeviceBuild) Checks.checkNotNull(deviceBuildAccessor);
        this.mInstr = (Instrumentation) Checks.checkNotNull(instr);
        this.mArgsBundle = (Bundle) Checks.checkNotNull(bundle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDeviceSdkInt() {
        return this.mDeviceBuild.getSdkVersionInt();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getDeviceHardware() {
        return this.mDeviceBuild.getHardware();
    }
}
