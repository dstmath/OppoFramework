package org.hamcrest.core;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class Every<T> extends TypeSafeDiagnosingMatcher<Iterable<? extends T>> {
    private final Matcher<? super T> matcher;

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: MutableMD:(java.lang.Iterable, org.hamcrest.Description):boolean
     arg types: [java.lang.Iterable<? extends T>, org.hamcrest.Description]
     candidates:
      org.hamcrest.core.Every.matchesSafely(java.lang.Object, org.hamcrest.Description):boolean
      MutableMD:(java.lang.Object, org.hamcrest.Description):boolean
      MutableMD:(java.lang.Iterable, org.hamcrest.Description):boolean */
    @Override // org.hamcrest.TypeSafeDiagnosingMatcher
    public /* bridge */ /* synthetic */ boolean matchesSafely(Object obj, Description description) {
        return matchesSafely((Iterable) ((Iterable) obj), description);
    }

    public Every(Matcher<? super T> matcher2) {
        this.matcher = matcher2;
    }

    public boolean matchesSafely(Iterable<? extends T> collection, Description mismatchDescription) {
        for (T t : collection) {
            if (!this.matcher.matches(t)) {
                mismatchDescription.appendText("an item ");
                this.matcher.describeMismatch(t, mismatchDescription);
                return false;
            }
        }
        return true;
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendText("every item is ").appendDescriptionOf(this.matcher);
    }

    public static <U> Matcher<Iterable<? extends U>> everyItem(Matcher<U> itemMatcher) {
        return new Every(itemMatcher);
    }
}
