package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.RemoteViews;
import java.util.TimeZone;

@RemoteViews.RemoteView
public class ColorDateTimeView extends DateTimeView {
    public ColorDateTimeView(Context context) {
        this(context, null);
    }

    @UnsupportedAppUsage
    public ColorDateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: package-private */
    public void update() {
        if (this.mTime != null && getVisibility() != 8) {
            ColorDateTimeView.super.update();
            if (isShowRelativeTime()) {
                updateColorRelativeTime();
            }
        }
    }

    private void updateColorRelativeTime() {
        String result;
        int i;
        int i2;
        int i3;
        int i4;
        long now = System.currentTimeMillis();
        long duration = Math.abs(now - this.mTimeMillis);
        boolean past = now >= this.mTimeMillis;
        if (duration >= 60000) {
            if (duration < 3600000) {
                int count = (int) (duration / 60000);
                Resources resources = getContext().getResources();
                if (past) {
                    i4 = 18153482;
                } else {
                    i4 = 18153483;
                }
                result = String.format(resources.getQuantityString(i4, count), Integer.valueOf(count));
            } else if (duration < 86400000) {
                int count2 = (int) (duration / 3600000);
                Resources resources2 = getContext().getResources();
                if (past) {
                    i3 = 18153478;
                } else {
                    i3 = 18153479;
                }
                result = String.format(resources2.getQuantityString(i3, count2), Integer.valueOf(count2));
            } else if (duration < 31449600000L) {
                int count3 = Math.max(Math.abs(dayDistance(TimeZone.getDefault(), this.mTimeMillis, now)), 1);
                Resources resources3 = getContext().getResources();
                if (past) {
                    i2 = 18153474;
                } else {
                    i2 = 18153475;
                }
                result = String.format(resources3.getQuantityString(i2, count3), Integer.valueOf(count3));
            } else {
                int count4 = (int) (duration / 31449600000L);
                Resources resources4 = getContext().getResources();
                if (past) {
                    i = 18153486;
                } else {
                    i = 18153487;
                }
                result = String.format(resources4.getQuantityString(i, count4), Integer.valueOf(count4));
            }
            setText(result);
        }
    }

    private static int dayDistance(TimeZone timeZone, long startTime, long endTime) {
        return Time.getJulianDay(endTime, (long) (timeZone.getOffset(endTime) / 1000)) - Time.getJulianDay(startTime, (long) (timeZone.getOffset(startTime) / 1000));
    }
}
