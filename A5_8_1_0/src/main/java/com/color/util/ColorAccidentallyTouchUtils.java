package com.color.util;

import android.app.OppoActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.FileObserver;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.OppoScreenDragUtil;
import android.view.View;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class ColorAccidentallyTouchUtils {
    private static final String ACCIDENTALLYTOUCH_FILE = "/data/oppo/coloros/oppoguardelf/sys_accidentally_touch_config_list.xml";
    private static final int DENSITY_XXHIGH = 3;
    private static final boolean PROPERTY_ENABLE = SystemProperties.getBoolean("debug.accidentally.touch", true);
    private static final String TAG = "ColorAccidentallyTouch";
    private static final String TAG_BEZEL = "bezel";
    private static final String TAG_BEZEL_AREA = "bezel_area";
    private static final String TAG_BEZEL_ENABLE = "bezel_enable";
    private static final String TAG_ENABLE = "enable_accidentallytouch";
    private static final String TAG_LEFT_OFFSET = "left_offset";
    private static final String TAG_POINT_LEFT_OFFSET = "point_left_offset";
    private static final String TAG_POINT_RIGHT_OFFSET = "point_right_offset";
    private static final String TAG_RIGHT_OFFSET = "right_offset";
    private static ColorAccidentallyTouchData mTouchData = null;
    private static ColorAccidentallyTouchUtils mTouchUtils = null;
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private FileObserverPolicy mAccidentallyTouchFileObserver = null;
    private final Object mAccidentallyTouchLock = new Object();
    private int mActivePointerId;
    private String mBezelArea = "7";
    private boolean mBezelEnable = true;
    private ArrayList<String> mBezelList = new ArrayList();
    private Context mContext;
    private int mDisplayWidth = 1080;
    private boolean mEnableAccidentallyTouch = true;
    private boolean mEnableMultiSence = true;
    private boolean mEnableSingleSence = true;
    private MotionEvent mExtraEvent;
    private boolean mIsMultiSence = false;
    private boolean mIsSingleSence = false;
    private boolean mIsWhiteApp = false;
    private int mLeftOffset = 5;
    private ArrayList<String> mMultiList = new ArrayList();
    private boolean mNeedExtraEvent;
    private int mPointLeftOffset = 80;
    private int mPointRightOffset = 80;
    private int mRightOffset = 5;
    private int mScrapPointerId;
    private ArrayList<String> mSingleList = new ArrayList();
    private boolean mSmallScreenMode = false;
    private ArrayList<String> mWhiteList = new ArrayList();
    private String mXmlEnable;
    private String mXmlLeftOffset;
    private List<String> mXmlMultiList = null;
    private String mXmlPointLeftOffset;
    private String mXmlPointRightOffset;
    private String mXmlRightOffset;
    private List<String> mXmlSingleList = null;
    private List<String> mXmlWhiteList = null;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (ColorAccidentallyTouchUtils.this.DEBUG) {
                Log.d(ColorAccidentallyTouchUtils.TAG, "FileObserver onEvent = " + event);
            }
            if (event == 8 && this.focusPath.equals(ColorAccidentallyTouchUtils.ACCIDENTALLYTOUCH_FILE)) {
                synchronized (ColorAccidentallyTouchUtils.this.mAccidentallyTouchLock) {
                    ColorAccidentallyTouchUtils.this.readConfigFile();
                }
            }
        }
    }

    private ColorAccidentallyTouchUtils() {
    }

    public static ColorAccidentallyTouchUtils getInstance() {
        if (mTouchUtils == null) {
            mTouchUtils = new ColorAccidentallyTouchUtils();
        }
        return mTouchUtils;
    }

    public void init() {
        initDir();
        initFileObserver();
        if (mTouchData == null) {
            mTouchData = new ColorAccidentallyTouchData();
        }
        this.mXmlSingleList = mTouchData.getSingleTouchList();
        this.mXmlMultiList = mTouchData.getMultiTouchList();
        this.mXmlWhiteList = mTouchData.getTouchWhiteList();
        synchronized (this.mAccidentallyTouchLock) {
            readConfigFile();
        }
    }

    public ColorAccidentallyTouchData getTouchData() {
        if (mTouchData == null) {
            mTouchData = new ColorAccidentallyTouchData();
        }
        return mTouchData;
    }

    private void initDir() {
        File file = new File(ACCIDENTALLYTOUCH_FILE);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readConfigFile() {
        Exception e;
        Throwable th;
        File file = new File(ACCIDENTALLYTOUCH_FILE);
        if (file.exists()) {
            this.mXmlSingleList.clear();
            this.mXmlMultiList.clear();
            this.mXmlWhiteList.clear();
            this.mBezelList.clear();
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, null);
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            String value;
                            String tag = parser.getName();
                            if ("o".equals(tag)) {
                                value = parser.getAttributeValue(null, "att");
                                if (this.DEBUG) {
                                    Log.d(TAG, "single-toush list : " + value);
                                }
                                if (value != null) {
                                    this.mXmlSingleList.add(value);
                                }
                            }
                            if ("p".equals(tag)) {
                                value = parser.getAttributeValue(null, "att");
                                if (this.DEBUG) {
                                    Log.d(TAG, "multi-touch list : " + value);
                                }
                                if (value != null) {
                                    this.mXmlMultiList.add(value);
                                }
                            }
                            if ("w".equals(tag)) {
                                value = parser.getAttributeValue(null, "att");
                                if (this.DEBUG) {
                                    Log.d(TAG, "white list : " + value);
                                }
                                if (value != null) {
                                    this.mXmlWhiteList.add(value);
                                }
                            }
                            if (TAG_BEZEL_ENABLE.equals(tag)) {
                                value = parser.nextText();
                                if (this.DEBUG) {
                                    Log.d(TAG, "bezel enable : " + value);
                                }
                                if (value != null) {
                                    if ("true".equals(value)) {
                                        this.mBezelEnable = true;
                                    } else {
                                        this.mBezelEnable = false;
                                    }
                                }
                            }
                            if (TAG_BEZEL.equals(tag)) {
                                value = parser.getAttributeValue(null, "att");
                                if (this.DEBUG) {
                                    Log.d(TAG, "bezel list : " + value);
                                }
                                if (value != null) {
                                    this.mBezelList.add(value);
                                }
                            }
                            if (TAG_BEZEL_AREA.equals(tag)) {
                                this.mBezelArea = parser.nextText();
                                if (this.DEBUG) {
                                    Log.d(TAG, "bezel_area = " + this.mBezelArea);
                                }
                            }
                            if (TAG_ENABLE.equals(tag)) {
                                this.mXmlEnable = parser.nextText();
                                if (this.DEBUG) {
                                    Log.d(TAG, "mXmlEnable = " + this.mXmlEnable);
                                }
                                mTouchData.setAccidentalltyTouchEnable(this.mXmlEnable);
                            }
                            if (TAG_LEFT_OFFSET.equals(tag)) {
                                this.mXmlLeftOffset = parser.nextText();
                                if (this.DEBUG) {
                                    Log.d(TAG, "mXmlLeftOffset = " + this.mXmlLeftOffset);
                                }
                                mTouchData.setLeftOffset(this.mXmlLeftOffset);
                            }
                            if (TAG_RIGHT_OFFSET.equals(tag)) {
                                this.mXmlRightOffset = parser.nextText();
                                if (this.DEBUG) {
                                    Log.d(TAG, "mXmlRightOffset = " + this.mXmlRightOffset);
                                }
                                mTouchData.setRightOffset(this.mXmlRightOffset);
                            }
                            if (TAG_POINT_LEFT_OFFSET.equals(tag)) {
                                this.mXmlPointLeftOffset = parser.nextText();
                                if (this.DEBUG) {
                                    Log.d(TAG, "mXmlPointLeftOffset = " + this.mXmlPointLeftOffset);
                                }
                                mTouchData.setPointLeftOffset(this.mXmlPointLeftOffset);
                            }
                            if (TAG_POINT_RIGHT_OFFSET.equals(tag)) {
                                this.mXmlPointRightOffset = parser.nextText();
                                if (this.DEBUG) {
                                    Log.d(TAG, "mXmlPointRightOffset = " + this.mXmlPointRightOffset);
                                }
                                mTouchData.setPointRightOffset(this.mXmlPointRightOffset);
                                continue;
                            } else {
                                continue;
                            }
                        }
                    } while (type != 1);
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    stream = stream2;
                } catch (Exception e3) {
                    e = e3;
                    stream = stream2;
                } catch (Throwable th2) {
                    th = th2;
                    stream = stream2;
                }
            } catch (Exception e4) {
                e = e4;
                try {
                    e.printStackTrace();
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        }
    }

    private void initFileObserver() {
        this.mAccidentallyTouchFileObserver = new FileObserverPolicy(ACCIDENTALLYTOUCH_FILE);
        this.mAccidentallyTouchFileObserver.startWatching();
    }

    public boolean isBezelEnable(String windowState) {
        if (!this.mBezelEnable || this.mBezelList == null || TextUtils.isEmpty(windowState)) {
            return false;
        }
        for (int i = 0; i < this.mBezelList.size(); i++) {
            if (windowState.contains((CharSequence) this.mBezelList.get(i))) {
                return true;
            }
        }
        return false;
    }

    public String getBezelArea() {
        return this.mBezelArea;
    }

    public MotionEvent getExtraEvent() {
        if (this.mNeedExtraEvent) {
            return this.mExtraEvent;
        }
        return null;
    }

    public MotionEvent getMotionEvent(MotionEvent event, View view) {
        this.mNeedExtraEvent = false;
        switch (event.getActionMasked()) {
            case 0:
                if (this.mContext != null) {
                    this.mDisplayWidth = this.mContext.getResources().getDisplayMetrics().widthPixels;
                }
                if (view == null) {
                    this.mEnableSingleSence = true;
                    this.mEnableMultiSence = true;
                }
                this.mIsSingleSence = false;
                this.mIsMultiSence = false;
                int downX = (int) event.getRawX();
                boolean isInEdge = (downX > this.mLeftOffset || downX < 0) ? downX >= this.mDisplayWidth - this.mRightOffset && downX <= this.mDisplayWidth : true;
                if (this.mEnableSingleSence && isInEdge) {
                    this.mIsSingleSence = true;
                    if (this.DEBUG) {
                        Log.d(TAG, "accidentally touch scrap 1 : " + event);
                    }
                    return null;
                }
                break;
            case 1:
                if (this.mIsSingleSence) {
                    this.mIsSingleSence = false;
                    if (this.DEBUG) {
                        Log.d(TAG, "accidentally touch scrap 3 : " + event);
                    }
                    return null;
                } else if (this.mIsMultiSence) {
                    if (this.mScrapPointerId == event.getPointerId(event.getActionIndex())) {
                        this.mIsMultiSence = false;
                        if (this.DEBUG) {
                            Log.d(TAG, "accidentally touch scrap 4 : " + event);
                        }
                        return null;
                    }
                }
                break;
            case 2:
                if (this.mIsSingleSence) {
                    int moveX = (int) event.getRawX();
                    boolean notEdge = moveX >= this.mLeftOffset && moveX <= this.mDisplayWidth - this.mRightOffset;
                    if (!notEdge) {
                        return null;
                    }
                    this.mIsSingleSence = false;
                    event.setAction(0);
                    if (this.DEBUG) {
                        Log.d(TAG, "accidentally touch dispatch 3 : " + event);
                    }
                }
                if (this.mIsMultiSence) {
                    if (event.getPointerCount() > 1) {
                        event = event.split(event.getPointerIdBits() - getFirstIds(event));
                        event.setDownTime(event.getEventTime());
                        break;
                    }
                    if (this.mScrapPointerId == event.getPointerId(event.getActionIndex())) {
                        return null;
                    }
                }
                break;
            case 5:
                int actionX = (int) event.getX(0);
                this.mIsMultiSence = false;
                boolean inEdge = (actionX < 0 || actionX > this.mPointLeftOffset) ? actionX >= this.mDisplayWidth - this.mPointRightOffset && actionX <= this.mDisplayWidth : true;
                if ((inEdge || this.mIsWhiteApp) && this.mEnableMultiSence && event.getPointerCount() > 1) {
                    this.mIsSingleSence = false;
                    this.mIsMultiSence = true;
                    if (event.getPointerCount() == 2) {
                        this.mScrapPointerId = event.getPointerId(0);
                        MotionEvent cancel = MotionEvent.obtain(event);
                        cancel.setAction(3);
                        if (this.DEBUG) {
                            Log.d(TAG, "accidentally touch add : " + cancel);
                        }
                        if (view != null) {
                            view.dispatchPointerEvent(cancel);
                        }
                        this.mNeedExtraEvent = true;
                        this.mExtraEvent = cancel;
                    }
                    event = event.split(event.getPointerIdBits() - getFirstIds(event));
                    event.setDownTime(event.getEventTime());
                    if (this.DEBUG) {
                        Log.d(TAG, "accidentally touch dispatch 1 : " + event);
                        break;
                    }
                }
                break;
            case 6:
                int pointerIndex = event.getActionIndex();
                if (!this.mIsMultiSence || this.mScrapPointerId != event.getPointerId(pointerIndex)) {
                    if (this.mIsMultiSence) {
                        event = event.split(event.getPointerIdBits() - getFirstIds(event));
                        event.setDownTime(event.getEventTime());
                        if (this.DEBUG) {
                            Log.d(TAG, "accidentally touch dispatch 2 : " + event);
                            break;
                        }
                    }
                }
                this.mIsMultiSence = false;
                if (this.DEBUG) {
                    Log.d(TAG, "accidentally touch scrap 2 : " + event);
                }
                return null;
                break;
        }
        return event;
    }

    public MotionEvent updatePointerEvent(MotionEvent event, View mView, Configuration mLastConfiguration) {
        if (event.getActionMasked() == 0) {
            this.mSmallScreenMode = OppoScreenDragUtil.isOffsetState();
        }
        if (event.getActionMasked() == 1) {
            this.mSmallScreenMode = false;
        }
        if (mLastConfiguration.orientation == 1 && this.mEnableAccidentallyTouch && (this.mSmallScreenMode ^ 1) != 0) {
            return getMotionEvent(event, mView);
        }
        return event;
    }

    public List<Object> handlePointerEvent(MotionEvent event, View mView, Configuration mLastConfiguration) {
        List<Object> list = new ArrayList();
        list.add(0, Integer.valueOf(0));
        list.add(1, event);
        return list;
    }

    public void initData(Context context) {
        try {
            this.mContext = context.getApplicationContext();
            if (this.mContext == null) {
                this.mContext = context;
            }
            ColorAccidentallyTouchData data = new OppoActivityManager().getAccidentallyTouchData();
            this.mSingleList = data.getSingleTouchList();
            this.mMultiList = data.getMultiTouchList();
            this.mWhiteList = data.getTouchWhiteList();
            String leftOffset = data.getLeftOffset();
            String rightOffset = data.getRightOffset();
            String pointLeftOffset = data.getPointLeftOffset();
            String pointRightOffset = data.getPointRightOffset();
            String enable = data.getAccidentalltyTouchEnable();
            if (leftOffset != null) {
                this.mLeftOffset = Integer.parseInt(leftOffset);
            }
            if (rightOffset != null) {
                this.mRightOffset = Integer.parseInt(rightOffset);
            }
            if (pointLeftOffset != null) {
                this.mPointLeftOffset = Integer.parseInt(pointLeftOffset);
            }
            if (pointRightOffset != null) {
                this.mPointRightOffset = Integer.parseInt(pointRightOffset);
            }
            if (enable != null) {
                if ("true".equals(enable)) {
                    this.mEnableAccidentallyTouch = true;
                } else {
                    this.mEnableAccidentallyTouch = false;
                }
            }
            this.mEnableSingleSence = isInList(this.mSingleList, context.getPackageName()) ^ 1;
            this.mEnableMultiSence = isInList(this.mMultiList, context.getPackageName()) ^ 1;
            this.mIsWhiteApp = isInList(this.mWhiteList, context.getPackageName());
            if (context.getPackageManager().isClosedSuperFirewall() || (PROPERTY_ENABLE ^ 1) != 0) {
                this.mEnableAccidentallyTouch = false;
            }
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            float scale = dm.density / 3.0f;
            this.mDisplayWidth = dm.widthPixels;
            this.mPointLeftOffset = (int) (((float) this.mPointLeftOffset) * scale);
            this.mPointRightOffset = (int) (((float) this.mPointRightOffset) * scale);
        } catch (RemoteException e) {
            Log.e(TAG, "init data RemoteException , " + e);
        } catch (Exception e2) {
            Log.e(TAG, "init data Exception , " + e2);
        } catch (Error e3) {
            Log.e(TAG, "init data Error , " + e3);
        }
    }

    private boolean isInList(ArrayList<String> list, String packageName) {
        if (list == null || (list.isEmpty() ^ 1) == 0 || !list.contains(packageName)) {
            return false;
        }
        return true;
    }

    private int getFirstIds(MotionEvent event) {
        if (event != null) {
            return 1 << event.getPointerId(0);
        }
        return 1;
    }
}
