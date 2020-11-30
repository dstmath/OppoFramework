package android.widget;

import android.content.Context;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OppoBaseView;
import com.color.util.ColorTypeCastingHelper;
import com.oppo.luckymoney.LMManager;

public abstract class OppoBaseAbsListView extends AdapterView<ListAdapter> {
    static final int EXCEPTION_NUM = 100;
    static final int EXCEPTION_TIME_GAP = 50;
    protected static final int FLYWHEEL_TIMEOUT_OPPO = 0;
    static final int[] LONG_FORMAT = {8224};
    private static final String TAG = "OppoBaseAbsListView";
    static long constantEndFlingNum = 0;
    static boolean isEnableEndFlingProtect = false;
    static long lastEndFlingTime = 0;
    protected float mFlingFriction = 1.06f;
    private OppoBaseView mOppoBaseView = ((OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, this));

    public abstract void colorStartSpringback();

    public abstract int getTouchMode();

    public abstract void setTouchMode(int i);

    public OppoBaseAbsListView(Context context) {
        super(context);
    }

    public OppoBaseAbsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean enableEndFlingProtectIfNeeded() {
        String packageName = getContext().getPackageName();
        if (!packageName.equals(LMManager.MM_PACKAGENAME) && !packageName.equals("gavin.example.abslistviewtest")) {
            return false;
        }
        isEnableEndFlingProtect = true;
        return true;
    }

    public void execEndFlingProtectIfNeeded() {
        if (isEnableEndFlingProtect) {
            long curTime = System.currentTimeMillis();
            if (curTime - lastEndFlingTime < 50) {
                constantEndFlingNum++;
                if (constantEndFlingNum >= 100) {
                    long[] oom_adj = new long[1];
                    int pid = Process.myPid();
                    Process.readProcFile("/proc/" + pid + "/oom_adj", LONG_FORMAT, null, oom_adj, null);
                    if (oom_adj[0] > 1) {
                        Log.d(TAG, "pid=" + pid + " killed");
                        Process.sendSignal(pid, 9);
                    } else {
                        Log.d(TAG, "waiting pid=" + pid + " to be background");
                    }
                }
            } else {
                constantEndFlingNum = 0;
            }
            lastEndFlingTime = curTime;
        }
    }

    /* JADX INFO: Multiple debug info for r0v3 android.widget.SpringOverScroller: [D('s' android.widget.SpringOverScroller), D('overScroller' android.widget.OverScroller)] */
    /* access modifiers changed from: protected */
    public OverScroller getOverScroller() {
        OppoBaseView oppoBaseView = this.mOppoBaseView;
        if (oppoBaseView == null || !oppoBaseView.isColorStyle()) {
            return new OverScroller(this.mContext);
        }
        SpringOverScroller s = new SpringOverScroller(this.mContext);
        s.setFlingFriction(this.mFlingFriction);
        return s;
    }

    public void setOppoFlingFriction(float f) {
        this.mFlingFriction = f;
    }

    public void setOppoFlingMode(int mode) {
        if (mode == 0) {
            setOppoFlingFriction(0.76f);
        } else if (mode != 1) {
            throw new IllegalArgumentException("wrong fling argument");
        }
    }
}
