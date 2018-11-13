package android.os;

import android.graphics.Paint;
import android.util.Log;

public class OppoAssertTip {
    private static final boolean DEBUG = false;
    private static final String PROP_ASSERT_TIP_RUNNING = "persist.sys.assert.state";
    private static final String TAG = "OppoAssertTip";
    private static OppoAssertTip mOppoAssertTipInstance = null;

    private native int setTipTextPaintAttr(int i, int i2);

    public native int getAssertMessageState();

    public native int hideAssertMessage();

    public native int showAssertMessage(String str);

    public native int testAddFunction(int i, int i2);

    private OppoAssertTip() {
    }

    public static OppoAssertTip getInstance() {
        if (mOppoAssertTipInstance == null) {
            mOppoAssertTipInstance = new OppoAssertTip();
        }
        return mOppoAssertTipInstance;
    }

    public void makeSureAssertTipServiceRunning() {
        if (!SystemProperties.getBoolean(PROP_ASSERT_TIP_RUNNING, false)) {
            SystemProperties.set(PROP_ASSERT_TIP_RUNNING, "true");
        }
    }

    public int requestShowAssertMessage(String message) {
        if (message != null && message.length() > 0) {
            return showAssertMessage(message);
        }
        Log.w(TAG, "requestShowAssertMessage:message is empty!");
        return -1;
    }

    public int requestSetTipTextPaintAttr(int textSize) {
        if (textSize < 10) {
            Log.w(TAG, "size is too small! set larger than 10.");
            return 0;
        }
        char[] str = new char[]{'W'};
        Paint testPaint = new Paint();
        testPaint.setTextSize((float) textSize);
        return setTipTextPaintAttr(textSize, (int) testPaint.measureText(str, 0, 1));
    }

    public boolean isAssertTipShowed() {
        if (getAssertMessageState() == 1) {
            return true;
        }
        return false;
    }
}
