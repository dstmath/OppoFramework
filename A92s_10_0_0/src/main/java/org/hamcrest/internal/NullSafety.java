package org.hamcrest.internal;

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsNull;

public class NullSafety {
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.util.ArrayList} */
    /* JADX WARN: Multi-variable type inference failed */
    public static <E> List<Matcher<? super E>> nullSafe(Matcher<? super E>[] itemMatchers) {
        List<Matcher<? super E>> matchers = new ArrayList<>(itemMatchers.length);
        int length = itemMatchers.length;
        for (int i = 0; i < length; i++) {
            Matcher<? super E> itemMatcher = itemMatchers[i];
            matchers.add(itemMatcher == null ? IsNull.nullValue() : itemMatcher);
        }
        return matchers;
    }
}
