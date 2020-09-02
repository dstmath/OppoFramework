package org.junit;

import org.hamcrest.Matcher;

public class AssumptionViolatedException extends org.junit.internal.AssumptionViolatedException {
    private static final long serialVersionUID = 1;

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: org.junit.internal.AssumptionViolatedException.<init>(java.lang.Object, org.hamcrest.Matcher<?>):void
     arg types: [T, org.hamcrest.Matcher<T>]
     candidates:
      org.junit.internal.AssumptionViolatedException.<init>(java.lang.String, java.lang.Throwable):void
      org.junit.internal.AssumptionViolatedException.<init>(java.lang.Object, org.hamcrest.Matcher<?>):void */
    public <T> AssumptionViolatedException(T actual, Matcher<T> matcher) {
        super((Object) actual, (Matcher<?>) matcher);
    }

    public <T> AssumptionViolatedException(String message, T expected, Matcher<T> matcher) {
        super(message, expected, matcher);
    }

    public AssumptionViolatedException(String message) {
        super(message);
    }

    public AssumptionViolatedException(String assumption, Throwable t) {
        super(assumption, t);
    }
}
