package androidx.test.internal.runner;

import android.app.Instrumentation;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.test.filters.RequiresDevice;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.Suppress;
import androidx.test.internal.runner.ClassPathScanner;
import androidx.test.internal.runner.RunnerArgs;
import androidx.test.internal.util.AndroidRunnerParams;
import androidx.test.internal.util.Checks;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class TestRequestBuilder {
    private static final String[] DEFAULT_EXCLUDED_PACKAGES = {"junit", "org.junit", "org.hamcrest", "org.mockito", "androidx.test.internal.runner.junit3", "org.jacoco", "net.bytebuddy"};
    private final Bundle argsBundle;
    private ClassLoader classLoader;
    private ClassAndMethodFilter classMethodFilter;
    private List<Class<? extends RunnerBuilder>> customRunnerBuilderClasses;
    private final DeviceBuild deviceBuild;
    private Set<String> excludedClasses;
    private Set<String> excludedPackages;
    private Filter filter;
    private boolean ignoreSuiteMethods;
    private Set<String> includedClasses;
    private Set<String> includedPackages;
    private final Instrumentation instr;
    private final List<String> pathsToScan;
    private long perTestTimeout;
    private boolean skipExecution;

    /* access modifiers changed from: package-private */
    public interface DeviceBuild {
        String getHardware();

        int getSdkVersionInt();
    }

    private static class DeviceBuildImpl implements DeviceBuild {
        private DeviceBuildImpl() {
        }

        @Override // androidx.test.internal.runner.TestRequestBuilder.DeviceBuild
        public int getSdkVersionInt() {
            return Build.VERSION.SDK_INT;
        }

        @Override // androidx.test.internal.runner.TestRequestBuilder.DeviceBuild
        public String getHardware() {
            return Build.HARDWARE;
        }
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

    /* access modifiers changed from: private */
    public static class AnnotationInclusionFilter extends ParentFilter {
        private final Class<? extends Annotation> annotationClass;

        AnnotationInclusionFilter(Class<? extends Annotation> annotation) {
            super();
            this.annotationClass = annotation;
        }

        /* access modifiers changed from: protected */
        @Override // androidx.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            Class<?> testClass = description.getTestClass();
            return description.getAnnotation(this.annotationClass) != null || (testClass != null && testClass.isAnnotationPresent(this.annotationClass));
        }
    }

    /* access modifiers changed from: private */
    public static class SizeFilter extends ParentFilter {
        private final TestSize testSize;

        SizeFilter(TestSize testSize2) {
            super();
            this.testSize = testSize2;
        }

        /* access modifiers changed from: protected */
        @Override // androidx.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            if (this.testSize.testMethodIsAnnotatedWithTestSize(description)) {
                return true;
            }
            if (!this.testSize.testClassIsAnnotatedWithTestSize(description)) {
                return false;
            }
            for (Annotation a : description.getAnnotations()) {
                if (TestSize.isAnyTestSize(a.annotationType())) {
                    return false;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class AnnotationExclusionFilter extends ParentFilter {
        private final Class<? extends Annotation> annotationClass;

        AnnotationExclusionFilter(Class<? extends Annotation> annotation) {
            super();
            this.annotationClass = annotation;
        }

        /* access modifiers changed from: protected */
        @Override // androidx.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            Class<?> testClass = description.getTestClass();
            if ((testClass == null || !testClass.isAnnotationPresent(this.annotationClass)) && description.getAnnotation(this.annotationClass) == null) {
                return true;
            }
            return false;
        }
    }

    private static class ExtendedSuite extends Suite {
        static Suite createSuite(List<Runner> runners) {
            try {
                return new ExtendedSuite(runners);
            } catch (InitializationError e) {
                String name = Suite.class.getName();
                StringBuilder sb = new StringBuilder(107 + String.valueOf(name).length());
                sb.append("Internal Error: ");
                sb.append(name);
                sb.append("(Class<?>, List<Runner>) should never throw an InitializationError when passed a null Class");
                throw new RuntimeException(sb.toString());
            }
        }

        ExtendedSuite(List<Runner> runners) throws InitializationError {
            super(null, runners);
        }
    }

    private class SdkSuppressFilter extends ParentFilter {
        private SdkSuppressFilter() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // androidx.test.internal.runner.TestRequestBuilder.ParentFilter
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
        @Override // androidx.test.internal.runner.TestRequestBuilder.AnnotationExclusionFilter, androidx.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            if (!super.evaluateTest(description)) {
                return !this.emulatorHardwareNames.contains(TestRequestBuilder.this.getDeviceHardware());
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ShardingFilter extends Filter {
        private final int numShards;
        private final int shardIndex;

        ShardingFilter(int numShards2, int shardIndex2) {
            this.numShards = numShards2;
            this.shardIndex = shardIndex2;
        }

        @Override // org.junit.runner.manipulation.Filter
        public boolean shouldRun(Description description) {
            if (!description.isTest() || Math.abs(description.hashCode()) % this.numShards == this.shardIndex) {
                return true;
            }
            return false;
        }
    }

    private static class LenientFilterRequest extends Request {
        private final Filter filter;
        private final Request request;

        public LenientFilterRequest(Request classRequest, Filter filter2) {
            this.request = classRequest;
            this.filter = filter2;
        }

        @Override // org.junit.runner.Request
        public Runner getRunner() {
            try {
                Runner runner = this.request.getRunner();
                this.filter.apply(runner);
                return runner;
            } catch (NoTestsRemainException e) {
                return new BlankRunner();
            }
        }
    }

    private static class BlankRunner extends Runner {
        private BlankRunner() {
        }

        @Override // org.junit.runner.Describable, org.junit.runner.Runner
        public Description getDescription() {
            return Description.createSuiteDescription("no tests found", new Annotation[0]);
        }

        @Override // org.junit.runner.Runner
        public void run(RunNotifier notifier) {
        }
    }

    /* access modifiers changed from: private */
    public static class ClassAndMethodFilter extends ParentFilter {
        private Map<String, MethodFilter> methodFilters;

        private ClassAndMethodFilter() {
            super();
            this.methodFilters = new HashMap();
        }

        @Override // androidx.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            MethodFilter methodFilter;
            if (!this.methodFilters.isEmpty() && (methodFilter = this.methodFilters.get(description.getClassName())) != null) {
                return methodFilter.shouldRun(description);
            }
            return true;
        }

        public void addMethod(String className, String methodName) {
            MethodFilter methodFilter = this.methodFilters.get(className);
            if (methodFilter == null) {
                methodFilter = new MethodFilter(className);
                this.methodFilters.put(className, methodFilter);
            }
            methodFilter.addInclusionMethod(methodName);
        }

        public void removeMethod(String className, String methodName) {
            MethodFilter methodFilter = this.methodFilters.get(className);
            if (methodFilter == null) {
                methodFilter = new MethodFilter(className);
                this.methodFilters.put(className, methodFilter);
            }
            methodFilter.addExclusionMethod(methodName);
        }
    }

    /* access modifiers changed from: private */
    public static class MethodFilter extends ParentFilter {
        private final String className;
        private Set<String> excludedMethods = new HashSet();
        private Set<String> includedMethods = new HashSet();

        public MethodFilter(String className2) {
            super();
            this.className = className2;
        }

        @Override // androidx.test.internal.runner.TestRequestBuilder.ParentFilter
        public boolean evaluateTest(Description description) {
            String methodName = description.getMethodName();
            if (methodName == null) {
                return false;
            }
            String methodName2 = stripParameterizedSuffix(methodName);
            if (this.excludedMethods.contains(methodName2)) {
                return false;
            }
            if (this.includedMethods.isEmpty() || this.includedMethods.contains(methodName2) || methodName2.equals("initializationError")) {
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

        public void addInclusionMethod(String methodName) {
            this.includedMethods.add(methodName);
        }

        public void addExclusionMethod(String methodName) {
            this.excludedMethods.add(methodName);
        }
    }

    public TestRequestBuilder(Instrumentation instr2, Bundle bundle) {
        this(new DeviceBuildImpl(), instr2, bundle);
    }

    TestRequestBuilder(DeviceBuild deviceBuildAccessor, Instrumentation instr2, Bundle bundle) {
        this.pathsToScan = new ArrayList();
        this.includedPackages = new HashSet();
        this.excludedPackages = new HashSet();
        this.includedClasses = new HashSet();
        this.excludedClasses = new HashSet();
        this.classMethodFilter = new ClassAndMethodFilter();
        this.filter = new AnnotationExclusionFilter(Suppress.class).intersect(new AnnotationExclusionFilter(android.test.suitebuilder.annotation.Suppress.class)).intersect(new SdkSuppressFilter()).intersect(new RequiresDeviceFilter()).intersect(this.classMethodFilter);
        this.customRunnerBuilderClasses = new ArrayList();
        this.skipExecution = false;
        this.perTestTimeout = 0;
        this.ignoreSuiteMethods = false;
        this.deviceBuild = (DeviceBuild) Checks.checkNotNull(deviceBuildAccessor);
        this.instr = (Instrumentation) Checks.checkNotNull(instr2);
        this.argsBundle = (Bundle) Checks.checkNotNull(bundle);
    }

    public TestRequestBuilder addPathsToScan(Iterable<String> paths) {
        for (String path : paths) {
            addPathToScan(path);
        }
        return this;
    }

    public TestRequestBuilder addPathToScan(String path) {
        this.pathsToScan.add(path);
        return this;
    }

    public TestRequestBuilder setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
        return this;
    }

    public TestRequestBuilder addTestClass(String className) {
        this.includedClasses.add(className);
        return this;
    }

    public TestRequestBuilder removeTestClass(String className) {
        this.excludedClasses.add(className);
        return this;
    }

    public TestRequestBuilder addTestMethod(String testClassName, String testMethodName) {
        this.includedClasses.add(testClassName);
        this.classMethodFilter.addMethod(testClassName, testMethodName);
        return this;
    }

    public TestRequestBuilder removeTestMethod(String testClassName, String testMethodName) {
        this.classMethodFilter.removeMethod(testClassName, testMethodName);
        return this;
    }

    public TestRequestBuilder addTestPackage(String testPackage) {
        this.includedPackages.add(testPackage);
        return this;
    }

    public TestRequestBuilder removeTestPackage(String testPackage) {
        this.excludedPackages.add(testPackage);
        return this;
    }

    public TestRequestBuilder addTestSizeFilter(TestSize forTestSize) {
        if (!TestSize.NONE.equals(forTestSize)) {
            addFilter(new SizeFilter(forTestSize));
        } else {
            Log.e("TestRequestBuilder", String.format("Unrecognized test size '%s'", forTestSize.getSizeQualifierName()));
        }
        return this;
    }

    public TestRequestBuilder addAnnotationInclusionFilter(String annotation) {
        Class<? extends Annotation> annotationClass = loadAnnotationClass(annotation);
        if (annotationClass != null) {
            addFilter(new AnnotationInclusionFilter(annotationClass));
        }
        return this;
    }

    public TestRequestBuilder addAnnotationExclusionFilter(String notAnnotation) {
        Class<? extends Annotation> annotationClass = loadAnnotationClass(notAnnotation);
        if (annotationClass != null) {
            addFilter(new AnnotationExclusionFilter(annotationClass));
        }
        return this;
    }

    public TestRequestBuilder addShardingFilter(int numShards, int shardIndex) {
        return addFilter(new ShardingFilter(numShards, shardIndex));
    }

    public TestRequestBuilder addFilter(Filter filter2) {
        this.filter = this.filter.intersect(filter2);
        return this;
    }

    public TestRequestBuilder addCustomRunnerBuilderClass(Class<? extends RunnerBuilder> runnerBuilderClass) {
        this.customRunnerBuilderClasses.add(runnerBuilderClass);
        return this;
    }

    public TestRequestBuilder setSkipExecution(boolean b) {
        this.skipExecution = b;
        return this;
    }

    public TestRequestBuilder setPerTestTimeout(long millis) {
        this.perTestTimeout = millis;
        return this;
    }

    public TestRequestBuilder addFromRunnerArgs(RunnerArgs runnerArgs) {
        for (RunnerArgs.TestArg test : runnerArgs.tests) {
            if (test.methodName == null) {
                addTestClass(test.testClassName);
            } else {
                addTestMethod(test.testClassName, test.methodName);
            }
        }
        for (RunnerArgs.TestArg test2 : runnerArgs.notTests) {
            if (test2.methodName == null) {
                removeTestClass(test2.testClassName);
            } else {
                removeTestMethod(test2.testClassName, test2.methodName);
            }
        }
        for (String pkg : runnerArgs.testPackages) {
            addTestPackage(pkg);
        }
        for (String pkg2 : runnerArgs.notTestPackages) {
            removeTestPackage(pkg2);
        }
        if (runnerArgs.testSize != null) {
            addTestSizeFilter(TestSize.fromString(runnerArgs.testSize));
        }
        if (runnerArgs.annotation != null) {
            addAnnotationInclusionFilter(runnerArgs.annotation);
        }
        for (String notAnnotation : runnerArgs.notAnnotations) {
            addAnnotationExclusionFilter(notAnnotation);
        }
        for (Filter filter2 : runnerArgs.filters) {
            addFilter(filter2);
        }
        if (runnerArgs.testTimeout > 0) {
            setPerTestTimeout(runnerArgs.testTimeout);
        }
        if (runnerArgs.numShards > 0 && runnerArgs.shardIndex >= 0 && runnerArgs.shardIndex < runnerArgs.numShards) {
            addShardingFilter(runnerArgs.numShards, runnerArgs.shardIndex);
        }
        if (runnerArgs.logOnly) {
            setSkipExecution(true);
        }
        if (runnerArgs.classLoader != null) {
            setClassLoader(runnerArgs.classLoader);
        }
        for (Class<? extends RunnerBuilder> runnerBuilderClass : runnerArgs.runnerBuilderClasses) {
            addCustomRunnerBuilderClass(runnerBuilderClass);
        }
        return this;
    }

    public Request build() {
        Collection<String> classNames;
        this.includedPackages.removeAll(this.excludedPackages);
        this.includedClasses.removeAll(this.excludedClasses);
        validate(this.includedClasses);
        boolean scanningPath = this.includedClasses.isEmpty();
        TestLoader loader = TestLoader.testLoader(this.classLoader, getRunnerBuilder(new AndroidRunnerParams(this.instr, this.argsBundle, this.perTestTimeout, this.ignoreSuiteMethods || scanningPath), scanningPath), scanningPath);
        if (scanningPath) {
            classNames = getClassNamesFromClassPath();
        } else {
            classNames = this.includedClasses;
        }
        return new LenientFilterRequest(Request.runner(ExtendedSuite.createSuite(loader.getRunnersFor(classNames, scanningPath))), this.filter);
    }

    private void validate(Set<String> classNames) {
        if (classNames.isEmpty() && this.pathsToScan.isEmpty()) {
            throw new IllegalArgumentException("Must provide either classes to run, or paths to scan");
        } else if ((!this.includedPackages.isEmpty() || !this.excludedPackages.isEmpty()) && !classNames.isEmpty()) {
            throw new IllegalArgumentException("Ambiguous arguments: cannot provide both test package and test class(es) to run");
        }
    }

    private RunnerBuilder getRunnerBuilder(AndroidRunnerParams runnerParams, boolean scanningPath) {
        if (this.skipExecution) {
            return new AndroidLogOnlyBuilder(runnerParams, scanningPath, this.customRunnerBuilderClasses);
        }
        return new AndroidRunnerBuilder(runnerParams, scanningPath, this.customRunnerBuilderClasses);
    }

    private Collection<String> getClassNamesFromClassPath() {
        if (!this.pathsToScan.isEmpty()) {
            Log.i("TestRequestBuilder", String.format("Scanning classpath to find tests in paths %s", this.pathsToScan));
            ClassPathScanner scanner = createClassPathScanner(this.pathsToScan);
            ClassPathScanner.ChainedClassNameFilter filter2 = new ClassPathScanner.ChainedClassNameFilter();
            filter2.add(new ClassPathScanner.ExternalClassNameFilter());
            String[] strArr = DEFAULT_EXCLUDED_PACKAGES;
            for (String pkg : strArr) {
                if (!this.includedPackages.contains(pkg)) {
                    this.excludedPackages.add(pkg);
                }
            }
            if (!this.includedPackages.isEmpty()) {
                filter2.add(new ClassPathScanner.InclusivePackageNamesFilter(this.includedPackages));
            }
            for (String pkg2 : this.excludedPackages) {
                filter2.add(new ClassPathScanner.ExcludePackageNameFilter(pkg2));
            }
            filter2.add(new ClassPathScanner.ExcludeClassNamesFilter(this.excludedClasses));
            try {
                return scanner.getClassPathEntries(filter2);
            } catch (IOException e) {
                Log.e("TestRequestBuilder", "Failed to scan classes", e);
                return Collections.emptyList();
            }
        } else {
            throw new IllegalStateException("neither test class to execute or class paths were provided");
        }
    }

    /* access modifiers changed from: package-private */
    public ClassPathScanner createClassPathScanner(List<String> classPath) {
        return new ClassPathScanner(classPath);
    }

    /* JADX DEBUG: Type inference failed for r2v2. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<? extends java.lang.annotation.Annotation> */
    private Class<? extends Annotation> loadAnnotationClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.e("TestRequestBuilder", String.format("Could not find annotation class: %s", className));
            return null;
        } catch (ClassCastException e2) {
            Log.e("TestRequestBuilder", String.format("Class %s is not an annotation", className));
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDeviceSdkInt() {
        return this.deviceBuild.getSdkVersionInt();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getDeviceHardware() {
        return this.deviceBuild.getHardware();
    }
}
