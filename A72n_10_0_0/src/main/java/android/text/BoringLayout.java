package android.text;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.TextUtils;
import android.text.style.ParagraphStyle;

public class BoringLayout extends Layout implements TextUtils.EllipsizeCallback {
    int mBottom;
    private int mBottomPadding;
    int mDesc;
    private String mDirect;
    private int mEllipsizedCount;
    private int mEllipsizedStart;
    private int mEllipsizedWidth;
    private float mMax;
    private Paint mPaint;
    private int mTopPadding;

    public static BoringLayout make(CharSequence source, TextPaint paint, int outerWidth, Layout.Alignment align, float spacingMult, float spacingAdd, Metrics metrics, boolean includePad) {
        return new BoringLayout(source, paint, outerWidth, align, spacingMult, spacingAdd, metrics, includePad);
    }

    public static BoringLayout make(CharSequence source, TextPaint paint, int outerWidth, Layout.Alignment align, float spacingmult, float spacingadd, Metrics metrics, boolean includePad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        return new BoringLayout(source, paint, outerWidth, align, spacingmult, spacingadd, metrics, includePad, ellipsize, ellipsizedWidth);
    }

    public BoringLayout replaceOrMake(CharSequence source, TextPaint paint, int outerwidth, Layout.Alignment align, float spacingMult, float spacingAdd, Metrics metrics, boolean includePad) {
        replaceWith(source, paint, outerwidth, align, spacingMult, spacingAdd);
        this.mEllipsizedWidth = outerwidth;
        this.mEllipsizedStart = 0;
        this.mEllipsizedCount = 0;
        init(source, paint, align, metrics, includePad, true);
        return this;
    }

    public BoringLayout replaceOrMake(CharSequence source, TextPaint paint, int outerWidth, Layout.Alignment align, float spacingMult, float spacingAdd, Metrics metrics, boolean includePad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        boolean trust;
        if (ellipsize == null || ellipsize == TextUtils.TruncateAt.MARQUEE) {
            replaceWith(source, paint, outerWidth, align, spacingMult, spacingAdd);
            this.mEllipsizedWidth = outerWidth;
            this.mEllipsizedStart = 0;
            this.mEllipsizedCount = 0;
            trust = true;
        } else {
            replaceWith(TextUtils.ellipsize(source, paint, (float) ellipsizedWidth, ellipsize, true, this), paint, outerWidth, align, spacingMult, spacingAdd);
            this.mEllipsizedWidth = ellipsizedWidth;
            trust = false;
        }
        init(getText(), paint, align, metrics, includePad, trust);
        return this;
    }

    public BoringLayout(CharSequence source, TextPaint paint, int outerwidth, Layout.Alignment align, float spacingMult, float spacingAdd, Metrics metrics, boolean includePad) {
        super(source, paint, outerwidth, align, spacingMult, spacingAdd);
        this.mEllipsizedWidth = outerwidth;
        this.mEllipsizedStart = 0;
        this.mEllipsizedCount = 0;
        init(source, paint, align, metrics, includePad, true);
    }

    public BoringLayout(CharSequence source, TextPaint paint, int outerWidth, Layout.Alignment align, float spacingMult, float spacingAdd, Metrics metrics, boolean includePad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        super(source, paint, outerWidth, align, spacingMult, spacingAdd);
        boolean trust;
        if (ellipsize == null || ellipsize == TextUtils.TruncateAt.MARQUEE) {
            this.mEllipsizedWidth = outerWidth;
            this.mEllipsizedStart = 0;
            this.mEllipsizedCount = 0;
            trust = true;
        } else {
            replaceWith(TextUtils.ellipsize(source, paint, (float) ellipsizedWidth, ellipsize, true, this), paint, outerWidth, align, spacingMult, spacingAdd);
            this.mEllipsizedWidth = ellipsizedWidth;
            trust = false;
        }
        init(getText(), paint, align, metrics, includePad, trust);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:18:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0022  */
    public void init(CharSequence source, TextPaint paint, Layout.Alignment align, Metrics metrics, boolean includePad, boolean trustWidth) {
        int spacing;
        if (source instanceof String) {
            if (align == Layout.Alignment.ALIGN_NORMAL) {
                this.mDirect = source.toString();
                this.mPaint = paint;
                if (!includePad) {
                    int spacing2 = metrics.bottom - metrics.top;
                    this.mDesc = metrics.bottom;
                    spacing = spacing2;
                } else {
                    int spacing3 = metrics.descent - metrics.ascent;
                    this.mDesc = metrics.descent;
                    spacing = spacing3;
                }
                this.mBottom = spacing;
                if (!trustWidth) {
                    this.mMax = (float) metrics.width;
                } else {
                    TextLine line = TextLine.obtain();
                    int length = source.length();
                    Layout.Directions directions = Layout.DIRS_ALL_LEFT_TO_RIGHT;
                    int i = this.mEllipsizedStart;
                    line.set(paint, source, 0, length, 1, directions, false, null, i, i + this.mEllipsizedCount);
                    this.mMax = (float) ((int) Math.ceil((double) line.metrics(null)));
                    TextLine.recycle(line);
                }
                if (!includePad) {
                    this.mTopPadding = metrics.top - metrics.ascent;
                    this.mBottomPadding = metrics.bottom - metrics.descent;
                    return;
                }
                return;
            }
        }
        this.mDirect = null;
        this.mPaint = paint;
        if (!includePad) {
        }
        this.mBottom = spacing;
        if (!trustWidth) {
        }
        if (!includePad) {
        }
    }

    public static Metrics isBoring(CharSequence text, TextPaint paint) {
        return isBoring(text, paint, TextDirectionHeuristics.FIRSTSTRONG_LTR, null);
    }

    public static Metrics isBoring(CharSequence text, TextPaint paint, Metrics metrics) {
        return isBoring(text, paint, TextDirectionHeuristics.FIRSTSTRONG_LTR, metrics);
    }

    /* JADX INFO: finally extract failed */
    private static boolean hasAnyInterestingChars(CharSequence text, int textLength) {
        char[] buffer = TextUtils.obtain(500);
        for (int start = 0; start < textLength; start += 500) {
            try {
                int end = Math.min(start + 500, textLength);
                TextUtils.getChars(text, start, end, buffer, 0);
                int len = end - start;
                for (int i = 0; i < len; i++) {
                    char c = buffer[i];
                    if (c == '\n' || c == '\t' || TextUtils.couldAffectRtl(c)) {
                        TextUtils.recycle(buffer);
                        return true;
                    }
                }
            } catch (Throwable th) {
                TextUtils.recycle(buffer);
                throw th;
            }
        }
        TextUtils.recycle(buffer);
        return false;
    }

    @UnsupportedAppUsage
    public static Metrics isBoring(CharSequence text, TextPaint paint, TextDirectionHeuristic textDir, Metrics metrics) {
        Metrics fm;
        int textLength = text.length();
        if (hasAnyInterestingChars(text, textLength)) {
            return null;
        }
        if (textDir != null && textDir.isRtl(text, 0, textLength)) {
            return null;
        }
        if ((text instanceof Spanned) && ((Spanned) text).getSpans(0, textLength, ParagraphStyle.class).length > 0) {
            return null;
        }
        if (metrics == null) {
            fm = new Metrics();
        } else {
            metrics.reset();
            fm = metrics;
        }
        TextLine line = TextLine.obtain();
        line.set(paint, text, 0, textLength, 1, Layout.DIRS_ALL_LEFT_TO_RIGHT, false, null, 0, 0);
        fm.width = (int) Math.ceil((double) line.metrics(fm));
        TextLine.recycle(line);
        return fm;
    }

    @Override // android.text.Layout
    public int getHeight() {
        return this.mBottom;
    }

    @Override // android.text.Layout
    public int getLineCount() {
        return 1;
    }

    @Override // android.text.Layout
    public int getLineTop(int line) {
        if (line == 0) {
            return 0;
        }
        return this.mBottom;
    }

    @Override // android.text.Layout
    public int getLineDescent(int line) {
        return this.mDesc;
    }

    @Override // android.text.Layout
    public int getLineStart(int line) {
        if (line == 0) {
            return 0;
        }
        return getText().length();
    }

    @Override // android.text.Layout
    public int getParagraphDirection(int line) {
        return 1;
    }

    @Override // android.text.Layout
    public boolean getLineContainsTab(int line) {
        return false;
    }

    @Override // android.text.Layout
    public float getLineMax(int line) {
        return this.mMax;
    }

    @Override // android.text.Layout
    public float getLineWidth(int line) {
        if (line == 0) {
            return this.mMax;
        }
        return 0.0f;
    }

    @Override // android.text.Layout
    public final Layout.Directions getLineDirections(int line) {
        return Layout.DIRS_ALL_LEFT_TO_RIGHT;
    }

    @Override // android.text.Layout
    public int getTopPadding() {
        return this.mTopPadding;
    }

    @Override // android.text.Layout
    public int getBottomPadding() {
        return this.mBottomPadding;
    }

    @Override // android.text.Layout
    public int getEllipsisCount(int line) {
        return this.mEllipsizedCount;
    }

    @Override // android.text.Layout
    public int getEllipsisStart(int line) {
        return this.mEllipsizedStart;
    }

    @Override // android.text.Layout
    public int getEllipsizedWidth() {
        return this.mEllipsizedWidth;
    }

    @Override // android.text.Layout
    public void draw(Canvas c, Path highlight, Paint highlightpaint, int cursorOffset) {
        String str = this.mDirect;
        if (str == null || highlight != null) {
            super.draw(c, highlight, highlightpaint, cursorOffset);
        } else {
            c.drawText(str, 0.0f, (float) (this.mBottom - this.mDesc), this.mPaint);
        }
    }

    @Override // android.text.TextUtils.EllipsizeCallback
    public void ellipsized(int start, int end) {
        this.mEllipsizedStart = start;
        this.mEllipsizedCount = end - start;
    }

    public static class Metrics extends Paint.FontMetricsInt {
        public int width;

        @Override // android.graphics.Paint.FontMetricsInt
        public String toString() {
            return super.toString() + " width=" + this.width;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void reset() {
            this.top = 0;
            this.bottom = 0;
            this.ascent = 0;
            this.descent = 0;
            this.width = 0;
            this.leading = 0;
        }
    }
}
