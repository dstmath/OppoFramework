package androidx.test.internal.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import junit.framework.TestCase;

public class AndroidRunnerBuilderUtil {
    public static boolean isJUnit3Test(Class<?> testClass) {
        return TestCase.class.isAssignableFrom(testClass);
    }

    public static boolean hasSuiteMethod(Class<?> testClass) {
        try {
            testClass.getMethod("suite", new Class[0]);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean hasJUnit3TestMethod(Class<?> loadedClass) {
        for (Method testMethod : loadedClass.getMethods()) {
            if (isPublicTestMethod(testMethod)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPublicTestMethod(Method m) {
        return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
    }

    private static boolean isTestMethod(Method m) {
        return m.getParameterTypes().length == 0 && m.getName().startsWith("test") && m.getReturnType().equals(Void.TYPE);
    }
}
