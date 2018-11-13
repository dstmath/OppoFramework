package android.widget;

import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.icu.util.Calendar;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import libcore.icu.DateUtilsBridge;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@RemoteView
public class DateTimeView extends TextView {
    private static final int SHOW_MONTH_DAY_YEAR = 1;
    private static final int SHOW_TIME = 0;
    private static final ThreadLocal<ReceiverInfo> sReceiverInfo = null;
    int mLastDisplay;
    DateFormat mLastFormat;
    private String mNowText;
    private boolean mShowRelativeTime;
    Date mTime;
    long mTimeMillis;
    private long mUpdateTimeMillis;

    private static class ReceiverInfo {
        private final ArrayList<DateTimeView> mAttachedViews;
        private final ContentObserver mObserver;
        private final BroadcastReceiver mReceiver;

        /* synthetic */ ReceiverInfo(ReceiverInfo receiverInfo) {
            this();
        }

        private ReceiverInfo() {
            this.mAttachedViews = new ArrayList();
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (!"android.intent.action.TIME_TICK".equals(intent.getAction()) || System.currentTimeMillis() >= ReceiverInfo.this.getSoonestUpdateTime()) {
                        ReceiverInfo.this.updateAll();
                    }
                }
            };
            this.mObserver = new ContentObserver(new Handler()) {
                public void onChange(boolean selfChange) {
                    ReceiverInfo.this.updateAll();
                }
            };
        }

        public void addView(DateTimeView v) {
            boolean register = this.mAttachedViews.isEmpty();
            this.mAttachedViews.add(v);
            if (register) {
                register(getApplicationContextIfAvailable(v.getContext()));
            }
        }

        public void removeView(DateTimeView v) {
            this.mAttachedViews.remove(v);
            if (this.mAttachedViews.isEmpty()) {
                unregister(getApplicationContextIfAvailable(v.getContext()));
            }
        }

        void updateAll() {
            int count = this.mAttachedViews.size();
            for (int i = 0; i < count; i++) {
                ((DateTimeView) this.mAttachedViews.get(i)).clearFormatAndUpdate();
            }
        }

        long getSoonestUpdateTime() {
            long result = Long.MAX_VALUE;
            int count = this.mAttachedViews.size();
            for (int i = 0; i < count; i++) {
                long time = ((DateTimeView) this.mAttachedViews.get(i)).mUpdateTimeMillis;
                if (time < result) {
                    result = time;
                }
            }
            return result;
        }

        static final Context getApplicationContextIfAvailable(Context context) {
            Context ac = context.getApplicationContext();
            return ac != null ? ac : ActivityThread.currentApplication().getApplicationContext();
        }

        void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_TICK");
            filter.addAction("android.intent.action.TIME_SET");
            filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
            filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            context.registerReceiver(this.mReceiver, filter);
        }

        void unregister(Context context) {
            context.unregisterReceiver(this.mReceiver);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.DateTimeView.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.DateTimeView.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.DateTimeView.<clinit>():void");
    }

    public DateTimeView(Context context) {
        this(context, null);
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLastDisplay = -1;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DateTimeView, 0, 0);
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            switch (a.getIndex(i)) {
                case 0:
                    setShowRelativeTime(a.getBoolean(i, false));
                    break;
                default:
                    break;
            }
        }
        a.recycle();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ReceiverInfo ri = (ReceiverInfo) sReceiverInfo.get();
        if (ri == null) {
            ri = new ReceiverInfo();
            sReceiverInfo.set(ri);
        }
        ri.addView(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ReceiverInfo ri = (ReceiverInfo) sReceiverInfo.get();
        if (ri != null) {
            ri.removeView(this);
        }
    }

    @RemotableViewMethod
    public void setTime(long time) {
        Time t = new Time();
        t.set(time);
        this.mTimeMillis = t.toMillis(false);
        this.mTime = new Date(t.year - 1900, t.month, t.monthDay, t.hour, t.minute, 0);
        update();
    }

    @RemotableViewMethod
    public void setShowRelativeTime(boolean showRelativeTime) {
        this.mShowRelativeTime = showRelativeTime;
        updateNowText();
        update();
    }

    @RemotableViewMethod
    public void setVisibility(int visibility) {
        boolean gotVisible = visibility != 8 && getVisibility() == 8;
        super.setVisibility(visibility);
        if (gotVisible) {
            update();
        }
    }

    void update() {
        if (this.mTime != null && getVisibility() != 8) {
            if (this.mShowRelativeTime) {
                updateRelativeTime();
                return;
            }
            int display;
            DateFormat format;
            Date time = this.mTime;
            Time t = new Time();
            t.set(this.mTimeMillis);
            t.second = 0;
            t.hour -= 12;
            long twelveHoursBefore = t.toMillis(false);
            t.hour += 12;
            long twelveHoursAfter = t.toMillis(false);
            t.hour = 0;
            t.minute = 0;
            long midnightBefore = t.toMillis(false);
            t.monthDay++;
            long midnightAfter = t.toMillis(false);
            t.set(System.currentTimeMillis());
            t.second = 0;
            long nowMillis = t.normalize(false);
            if ((nowMillis < midnightBefore || nowMillis >= midnightAfter) && (nowMillis < twelveHoursBefore || nowMillis >= twelveHoursAfter)) {
                display = 1;
            } else {
                display = 0;
            }
            if (display != this.mLastDisplay || this.mLastFormat == null) {
                switch (display) {
                    case 0:
                        format = getTimeFormat();
                        break;
                    case 1:
                        format = DateFormat.getDateInstance(3);
                        break;
                    default:
                        throw new RuntimeException("unknown display value: " + display);
                }
                this.mLastFormat = format;
            } else {
                format = this.mLastFormat;
            }
            setText((CharSequence) format.format(this.mTime));
            if (display == 0) {
                if (twelveHoursAfter <= midnightAfter) {
                    twelveHoursAfter = midnightAfter;
                }
                this.mUpdateTimeMillis = twelveHoursAfter;
            } else if (this.mTimeMillis < nowMillis) {
                this.mUpdateTimeMillis = 0;
            } else {
                if (twelveHoursBefore >= midnightBefore) {
                    twelveHoursBefore = midnightBefore;
                }
                this.mUpdateTimeMillis = twelveHoursBefore;
            }
        }
    }

    private void updateRelativeTime() {
        long now = System.currentTimeMillis();
        long duration = Math.abs(now - this.mTimeMillis);
        boolean past = now >= this.mTimeMillis;
        if (duration < 60000) {
            setText(this.mNowText);
            this.mUpdateTimeMillis = (this.mTimeMillis + 60000) + 1;
            return;
        }
        int count;
        String result;
        long millisIncrease;
        Resources resources;
        int i;
        String quantityString;
        Object[] objArr;
        if (duration < DateUtils.HOUR_IN_MILLIS) {
            count = (int) (duration / 60000);
            resources = getContext().getResources();
            if (past) {
                i = R.plurals.duration_minutes_shortest;
            } else {
                i = R.plurals.duration_minutes_shortest_future;
            }
            quantityString = resources.getQuantityString(i, count);
            objArr = new Object[1];
            objArr[0] = Integer.valueOf(count);
            result = String.format(quantityString, objArr);
            millisIncrease = 60000;
        } else if (duration < DateUtils.DAY_IN_MILLIS) {
            count = (int) (duration / DateUtils.HOUR_IN_MILLIS);
            resources = getContext().getResources();
            if (past) {
                i = R.plurals.duration_hours_shortest;
            } else {
                i = R.plurals.duration_hours_shortest_future;
            }
            quantityString = resources.getQuantityString(i, count);
            objArr = new Object[1];
            objArr[0] = Integer.valueOf(count);
            result = String.format(quantityString, objArr);
            millisIncrease = DateUtils.HOUR_IN_MILLIS;
        } else if (duration < DateUtils.YEAR_IN_MILLIS) {
            TimeZone timeZone = TimeZone.getDefault();
            count = Math.max(Math.abs(dayDistance(timeZone, this.mTimeMillis, now)), 1);
            resources = getContext().getResources();
            if (past) {
                i = R.plurals.duration_days_shortest;
            } else {
                i = R.plurals.duration_days_shortest_future;
            }
            quantityString = resources.getQuantityString(i, count);
            objArr = new Object[1];
            objArr[0] = Integer.valueOf(count);
            result = String.format(quantityString, objArr);
            if (past || count != 1) {
                this.mUpdateTimeMillis = computeNextMidnight(timeZone);
                millisIncrease = -1;
            } else {
                millisIncrease = DateUtils.DAY_IN_MILLIS;
            }
        } else {
            count = (int) (duration / DateUtils.YEAR_IN_MILLIS);
            resources = getContext().getResources();
            if (past) {
                i = R.plurals.duration_years_shortest;
            } else {
                i = R.plurals.duration_years_shortest_future;
            }
            quantityString = resources.getQuantityString(i, count);
            objArr = new Object[1];
            objArr[0] = Integer.valueOf(count);
            result = String.format(quantityString, objArr);
            millisIncrease = DateUtils.YEAR_IN_MILLIS;
        }
        if (millisIncrease != -1) {
            if (past) {
                this.mUpdateTimeMillis = (this.mTimeMillis + (((long) (count + 1)) * millisIncrease)) + 1;
            } else {
                this.mUpdateTimeMillis = (this.mTimeMillis - (((long) count) * millisIncrease)) + 1;
            }
        }
        setText((CharSequence) result);
    }

    private long computeNextMidnight(TimeZone timeZone) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(DateUtilsBridge.icuTimeZone(timeZone));
        c.add(5, 1);
        c.set(11, 0);
        c.set(12, 0);
        c.set(13, 0);
        c.set(14, 0);
        return c.getTimeInMillis();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateNowText();
        update();
    }

    private void updateNowText() {
        if (this.mShowRelativeTime) {
            this.mNowText = getContext().getResources().getString(R.string.now_string_shortest);
        }
    }

    private static int dayDistance(TimeZone timeZone, long startTime, long endTime) {
        return Time.getJulianDay(endTime, (long) (timeZone.getOffset(endTime) / 1000)) - Time.getJulianDay(startTime, (long) (timeZone.getOffset(startTime) / 1000));
    }

    private DateFormat getTimeFormat() {
        return android.text.format.DateFormat.getTimeFormat(getContext());
    }

    void clearFormatAndUpdate() {
        this.mLastFormat = null;
        update();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (this.mShowRelativeTime) {
            String result;
            long now = System.currentTimeMillis();
            long duration = Math.abs(now - this.mTimeMillis);
            boolean past = now >= this.mTimeMillis;
            int count;
            Resources resources;
            int i;
            String quantityString;
            Object[] objArr;
            if (duration < 60000) {
                result = this.mNowText;
            } else if (duration < DateUtils.HOUR_IN_MILLIS) {
                count = (int) (duration / 60000);
                resources = getContext().getResources();
                if (past) {
                    i = R.plurals.duration_minutes_relative;
                } else {
                    i = R.plurals.duration_minutes_relative_future;
                }
                quantityString = resources.getQuantityString(i, count);
                objArr = new Object[1];
                objArr[0] = Integer.valueOf(count);
                result = String.format(quantityString, objArr);
            } else if (duration < DateUtils.DAY_IN_MILLIS) {
                count = (int) (duration / DateUtils.HOUR_IN_MILLIS);
                resources = getContext().getResources();
                if (past) {
                    i = R.plurals.duration_hours_relative;
                } else {
                    i = R.plurals.duration_hours_relative_future;
                }
                quantityString = resources.getQuantityString(i, count);
                objArr = new Object[1];
                objArr[0] = Integer.valueOf(count);
                result = String.format(quantityString, objArr);
            } else if (duration < DateUtils.YEAR_IN_MILLIS) {
                count = Math.max(Math.abs(dayDistance(TimeZone.getDefault(), this.mTimeMillis, now)), 1);
                resources = getContext().getResources();
                if (past) {
                    i = R.plurals.duration_days_relative;
                } else {
                    i = R.plurals.duration_days_relative_future;
                }
                quantityString = resources.getQuantityString(i, count);
                objArr = new Object[1];
                objArr[0] = Integer.valueOf(count);
                result = String.format(quantityString, objArr);
            } else {
                count = (int) (duration / DateUtils.YEAR_IN_MILLIS);
                resources = getContext().getResources();
                if (past) {
                    i = R.plurals.duration_years_relative;
                } else {
                    i = R.plurals.duration_years_relative_future;
                }
                quantityString = resources.getQuantityString(i, count);
                objArr = new Object[1];
                objArr[0] = Integer.valueOf(count);
                result = String.format(quantityString, objArr);
            }
            info.setText(result);
        }
    }
}
