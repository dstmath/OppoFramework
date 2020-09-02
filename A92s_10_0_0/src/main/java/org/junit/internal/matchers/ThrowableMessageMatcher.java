package org.junit.internal.matchers;

import java.lang.Throwable;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ThrowableMessageMatcher<T extends Throwable> extends TypeSafeMatcher<T> {
    private final Matcher<String> matcher;

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(java.lang.Throwable, org.hamcrest.Description):void
     arg types: [T, org.hamcrest.Description]
     candidates:
      org.junit.internal.matchers.ThrowableMessageMatcher.describeMismatchSafely(java.lang.Object, org.hamcrest.Description):void
      MutableMD:(java.lang.Object, org.hamcrest.Description):void
      MutableMD:(java.lang.Throwable, org.hamcrest.Description):void */
    /* access modifiers changed from: protected */
    @Override // org.hamcrest.TypeSafeMatcher
    public /* bridge */ /* synthetic */ void describeMismatchSafely(Object obj, Description description) {
        describeMismatchSafely((Throwable) ((Throwable) obj), description);
    }

    /* access modifiers changed from: protected */
    @Override // org.hamcrest.TypeSafeMatcher
    public /* bridge */ /* synthetic */ boolean matchesSafely(Object obj) {
        return matchesSafely((Throwable) ((Throwable) obj));
    }

    public ThrowableMessageMatcher(Matcher<String> matcher2) {
        this.matcher = matcher2;
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendText("exception with message ");
        description.appendDescriptionOf(this.matcher);
    }

    /* access modifiers changed from: protected */
    public boolean matchesSafely(T item) {
        return this.matcher.matches(item.getMessage());
    }

    /* access modifiers changed from: protected */
    public void describeMismatchSafely(T item, Description description) {
        description.appendText("message ");
        this.matcher.describeMismatch(item.getMessage(), description);
    }

    @Factory
    public static <T extends Throwable> Matcher<T> hasMessage(Matcher<String> matcher2) {
        return new ThrowableMessageMatcher(matcher2);
    }
}
