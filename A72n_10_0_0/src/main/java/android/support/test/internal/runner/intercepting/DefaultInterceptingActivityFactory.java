package android.support.test.internal.runner.intercepting;

import android.app.Activity;
import android.content.Intent;
import android.support.test.runner.intercepting.InterceptingActivityFactory;

public final class DefaultInterceptingActivityFactory implements InterceptingActivityFactory {
    @Override // android.support.test.runner.intercepting.InterceptingActivityFactory
    public boolean shouldIntercept(ClassLoader classLoader, String className, Intent intent) {
        return false;
    }

    @Override // android.support.test.runner.intercepting.InterceptingActivityFactory
    public Activity create(ClassLoader classLoader, String className, Intent intent) {
        throw new UnsupportedOperationException();
    }
}
