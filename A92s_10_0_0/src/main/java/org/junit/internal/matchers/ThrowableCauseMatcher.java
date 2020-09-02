package org.junit.internal.matchers;

import java.lang.Throwable;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ThrowableCauseMatcher<T extends Throwable> extends TypeSafeMatcher<T> {
    private final Matcher<? extends Throwable> causeMatcher;

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(java.lang.Throwable, org.hamcrest.Description):void
     arg types: [T, org.hamcrest.Description]
     candidates:
      org.junit.internal.matchers.ThrowableCauseMatcher.describeMismatchSafely(java.lang.Object, org.hamcrest.Description):void
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

    public ThrowableCauseMatcher(Matcher<? extends Throwable> causeMatcher2) {
        this.causeMatcher = causeMatcher2;
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendText("exception with cause ");
        description.appendDescriptionOf(this.causeMatcher);
    }

    /* access modifiers changed from: protected */
    public boolean matchesSafely(T item) {
        return this.causeMatcher.matches(item.getCause());
    }

    /* access modifiers changed from: protected */
    public void describeMismatchSafely(T item, Description description) {
        description.appendText("cause ");
        this.causeMatcher.describeMismatch(item.getCause(), description);
    }

    @Factory
    public static <T extends Throwable> Matcher<T> hasCause(Matcher<? extends Throwable> matcher) {
        return new ThrowableCauseMatcher(matcher);
    }
}
