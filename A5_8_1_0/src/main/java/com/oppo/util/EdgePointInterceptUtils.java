package com.oppo.util;

import android.os.SystemProperties;
import android.view.MotionEvent;
import android.view.OppoScreenDragUtil;
import javax.microedition.khronos.opengles.GL10;

public class EdgePointInterceptUtils {
    private static final String EDGE_SWITCH = "sys.oppo.curvedscreen.EdgeSuppression";
    private static final int EDGE_TIME = 300;
    private static final float EVENT_270 = 1.5707964f;
    private static final float EVENT_90 = -1.5707964f;
    private static final float EVENT_ORI = 0.0f;
    private static final float EVENT_OTHER = -3.1415927f;
    private static final int HEIGHT = 2340;
    private static final int MUTIL_POINT = 2;
    private static final int NAVI_GESTURE = 144;
    private static final int T1 = 32;
    private static final int T2 = 50;
    private static final String TAG = "EdgePointInterceptUtils";
    private static final int WIDTH = 1080;
    private static EdgePointInterceptUtils mIns;
    private int mCount = 0;
    private long mDownTime = 0;
    private boolean mEdgeSwitch = false;
    private MotionEvent mEvent;
    private int mIdBits = 0;
    private boolean mIsDownEdge = false;
    private boolean mIsLastPointUp = false;
    private boolean mIsPointUp = false;
    private boolean mIsTimeUp = false;
    private int mLastCount = 0;
    private MotionEvent mLastEvent;
    private int mLastIdBits = 0;
    private float mOri = EVENT_ORI;
    private boolean mSmallScreenMode = false;
    private long mSplitDownTime = 0;
    private int[] map = new int[]{1, 2, 4, 8, 16, 32, 64, 128, GL10.GL_DEPTH_BUFFER_BIT, 512, 1024, 2048};

    public MotionEvent splitEvent(MotionEvent event) {
        if (event != null) {
            if (disableSplit(event)) {
                return event;
            }
            int downY = (int) event.getRawY();
            int downX = (int) event.getRawX();
            this.mOri = event.getOrientation();
            this.mLastCount = this.mCount;
            this.mIsLastPointUp = this.mIsPointUp;
            this.mLastIdBits = this.mIdBits;
            switch (event.getActionMasked()) {
                case 0:
                    reset();
                    this.mDownTime = event.getDownTime();
                    if (this.mOri == EVENT_ORI || this.mOri == EVENT_OTHER) {
                        if (downX <= 32 || downX >= 1048) {
                            this.mIsDownEdge = true;
                            return null;
                        }
                        this.mLastCount++;
                        this.mCount++;
                        setPointDown(event, downY);
                        return event;
                    } else if (this.mOri == EVENT_90 || this.mOri == EVENT_270) {
                        if (downY <= 32 || downY >= 1048) {
                            this.mIsDownEdge = true;
                            return null;
                        }
                        this.mLastCount++;
                        this.mCount++;
                        setPointDown(event, downX);
                        return event;
                    }
                    break;
                case 1:
                case 3:
                case 4:
                    this.mIsPointUp = false;
                    this.mIdBits = getPointIdBits(event);
                    if (this.mIdBits > 0) {
                        this.mEvent = event.split(this.mIdBits);
                        setDownTime();
                        return this.mEvent;
                    }
                    break;
                case 2:
                    this.mIsPointUp = false;
                    if (!this.mIsTimeUp && event.getEventTime() - this.mDownTime > 300) {
                        this.mIsTimeUp = true;
                    }
                    this.mIdBits = getPointIdBits(event);
                    if (this.mIdBits > 0) {
                        this.mEvent = event.split(this.mIdBits);
                        setDownTime();
                        setAction();
                        return this.mEvent;
                    } else if (!(this.mLastCount != 1 || (this.mIsLastPointUp ^ 1) == 0 || this.mLastEvent == null)) {
                        this.mLastEvent.setAction(1);
                        this.mLastCount = 0;
                        return this.mLastEvent;
                    }
                case 5:
                    this.mIsPointUp = false;
                    this.mIdBits = getPointIdBits(event);
                    if (this.mIdBits > 0) {
                        this.mEvent = event.split(this.mIdBits);
                        if (this.mEvent != null && this.mEvent.getActionMasked() == 0) {
                            this.mSplitDownTime = this.mEvent.getEventTime();
                        }
                        setDownTime();
                        this.mLastEvent = MotionEvent.obtain(this.mEvent);
                        return this.mEvent;
                    }
                    break;
                case 6:
                    this.mIsPointUp = true;
                    this.mIdBits = getPointIdBits(event);
                    if (this.mIdBits > 0) {
                        this.mEvent = event.split(this.mIdBits);
                        setDownTime();
                        return this.mEvent;
                    }
                    break;
                default:
                    return event;
            }
        }
        return null;
    }

    private int getPointIdBits(MotionEvent event) {
        int ids = 0;
        int count = event.getPointerCount();
        boolean isPortrait = false;
        this.mCount = 0;
        int edge = this.mIsDownEdge ? 50 : 32;
        if (this.mIsTimeUp) {
            edge = 32;
        }
        if (this.mOri == EVENT_ORI || this.mOri == EVENT_OTHER) {
            isPortrait = true;
        }
        for (int i = 0; i < count; i++) {
            int x;
            if (isPortrait) {
                x = (int) event.getX(i);
            } else {
                x = (int) event.getY(i);
            }
            if (x > edge && x < 1080 - edge) {
                this.mCount++;
                ids |= 1 << event.getPointerId(i);
            }
        }
        return ids;
    }

    private EdgePointInterceptUtils() {
    }

    private void reset() {
        this.mIsDownEdge = false;
        this.mIsLastPointUp = false;
        this.mIsPointUp = false;
        this.mLastCount = 0;
        this.mCount = 0;
        this.mLastIdBits = 0;
        this.mIdBits = 0;
        this.mIsTimeUp = false;
        this.mDownTime = 0;
        this.mSplitDownTime = 0;
    }

    private void setAction() {
        if (this.mEvent != null) {
            if (this.mCount + this.mLastCount > 2) {
                if (this.mCount > this.mLastCount) {
                    this.mEvent.setAction((getIndex(this.mEvent) << 8) | 5);
                } else if (this.mCount >= this.mLastCount) {
                    this.mLastEvent = MotionEvent.obtain(this.mEvent);
                } else if (!(this.mIsLastPointUp || this.mLastEvent == null)) {
                    int pointIndex = getIndex(this.mLastEvent);
                    this.mEvent = this.mLastEvent;
                    this.mEvent.setAction((pointIndex << 8) | 6);
                }
            } else if (this.mCount > this.mLastCount) {
                this.mEvent.setAction(0);
                this.mSplitDownTime = this.mEvent.getEventTime();
                this.mEvent.setDownTime(this.mSplitDownTime);
            } else if (this.mCount < this.mLastCount) {
                this.mEvent.setAction(1);
            } else {
                this.mLastEvent = MotionEvent.obtain(this.mEvent);
            }
        }
    }

    public static EdgePointInterceptUtils getInstance() {
        if (mIns == null) {
            mIns = new EdgePointInterceptUtils();
        }
        return mIns;
    }

    private int getId() {
        int bits = this.mLastIdBits - this.mIdBits;
        if (bits < 0) {
            bits = -bits;
        }
        if (bits == 0) {
            return 0;
        }
        for (int i = 0; i < this.map.length; i++) {
            if (this.map[i] == bits) {
                return i;
            }
        }
        return 0;
    }

    private int getIndex(MotionEvent event) {
        if (event != null) {
            int pointCount = event.getPointerCount();
            int id = getId();
            for (int i = 0; i < pointCount; i++) {
                if (id == event.getPointerId(i)) {
                    return i;
                }
            }
        }
        return 0;
    }

    private void setDownTime() {
        if (this.mSplitDownTime != 0 && this.mEvent != null) {
            this.mEvent.setDownTime(this.mSplitDownTime);
        }
    }

    private boolean disableSplit(MotionEvent event) {
        if (event.getActionMasked() == 0) {
            this.mSmallScreenMode = OppoScreenDragUtil.isOffsetState();
            this.mEdgeSwitch = SystemProperties.getBoolean(EDGE_SWITCH, false);
        }
        return !this.mSmallScreenMode ? this.mEdgeSwitch ^ 1 : true;
    }

    private void setPointDown(MotionEvent event, int pointX) {
        if (event == null) {
            return;
        }
        if (pointX < NAVI_GESTURE || pointX > 2196) {
            this.mSplitDownTime = event.getEventTime();
            if (Math.abs(this.mSplitDownTime - event.getDownTime()) != 1) {
                event.setDownTime(this.mSplitDownTime);
                return;
            }
            return;
        }
        this.mSplitDownTime = event.getEventTime();
        event.setDownTime(this.mSplitDownTime);
    }
}
