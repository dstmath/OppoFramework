package android.text;

import android.annotation.UnsupportedAppUsage;
import java.lang.reflect.Array;
import java.util.Arrays;

public class SpanSet<E> {
    private final Class<? extends E> classType;
    int numberOfSpans = 0;
    int[] spanEnds;
    int[] spanFlags;
    int[] spanStarts;
    @UnsupportedAppUsage
    E[] spans;

    SpanSet(Class<? extends E> type) {
        this.classType = type;
    }

    public void init(Spanned spanned, int start, int limit) {
        E[] eArr;
        E[] allSpans = spanned.getSpans(start, limit, this.classType);
        int length = allSpans.length;
        if (length > 0 && ((eArr = this.spans) == null || eArr.length < length)) {
            this.spans = (Object[]) Array.newInstance(this.classType, length);
            this.spanStarts = new int[length];
            this.spanEnds = new int[length];
            this.spanFlags = new int[length];
        }
        int prevNumberOfSpans = this.numberOfSpans;
        this.numberOfSpans = 0;
        for (E span : allSpans) {
            int spanStart = spanned.getSpanStart(span);
            int spanEnd = spanned.getSpanEnd(span);
            if (spanStart != spanEnd) {
                int spanFlag = spanned.getSpanFlags(span);
                E[] eArr2 = this.spans;
                int i = this.numberOfSpans;
                eArr2[i] = span;
                this.spanStarts[i] = spanStart;
                this.spanEnds[i] = spanEnd;
                this.spanFlags[i] = spanFlag;
                this.numberOfSpans = i + 1;
            }
        }
        int i2 = this.numberOfSpans;
        if (i2 < prevNumberOfSpans) {
            Arrays.fill(this.spans, i2, prevNumberOfSpans, (Object) null);
        }
    }

    public boolean hasSpansIntersecting(int start, int end) {
        for (int i = 0; i < this.numberOfSpans; i++) {
            if (this.spanStarts[i] < end && this.spanEnds[i] > start) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public int getNextTransition(int start, int limit) {
        for (int i = 0; i < this.numberOfSpans; i++) {
            int spanStart = this.spanStarts[i];
            int spanEnd = this.spanEnds[i];
            if (spanStart > start && spanStart < limit) {
                limit = spanStart;
            }
            if (spanEnd > start && spanEnd < limit) {
                limit = spanEnd;
            }
        }
        return limit;
    }

    public void recycle() {
        E[] eArr = this.spans;
        if (eArr != null) {
            Arrays.fill(eArr, 0, this.numberOfSpans, (Object) null);
        }
    }
}
