package org.junit.internal.matchers;

import java.lang.reflect.Method;
import org.hamcrest.BaseMatcher;
import org.junit.internal.MethodSorter;

@Deprecated
public abstract class TypeSafeMatcher<T> extends BaseMatcher<T> {
    private Class<?> expectedType;

    public abstract boolean matchesSafely(T t);

    protected TypeSafeMatcher() {
        this.expectedType = findExpectedType(getClass());
    }

    private static Class<?> findExpectedType(Class<?> fromClass) {
        for (Class<?> c = fromClass; c != Object.class; c = c.getSuperclass()) {
            Method[] declaredMethods = MethodSorter.getDeclaredMethods(c);
            for (Method method : declaredMethods) {
                if (isMatchesSafelyMethod(method)) {
                    return method.getParameterTypes()[0];
                }
            }
        }
        throw new Error("Cannot determine correct type for matchesSafely() method.");
    }

    private static boolean isMatchesSafelyMethod(Method method) {
        if (!method.getName().equals("matchesSafely") || method.getParameterTypes().length != 1 || method.isSynthetic()) {
            return false;
        }
        return true;
    }

    protected TypeSafeMatcher(Class<T> expectedType2) {
        this.expectedType = expectedType2;
    }

    @Override // org.hamcrest.Matcher
    public final boolean matches(Object item) {
        return item != null && this.expectedType.isInstance(item) && matchesSafely(item);
    }
}
