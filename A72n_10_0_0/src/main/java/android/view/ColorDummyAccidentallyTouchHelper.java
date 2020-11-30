package android.view;

import android.content.Context;
import android.content.res.Configuration;
import com.color.util.ColorAccidentallyTouchData;

public class ColorDummyAccidentallyTouchHelper implements IColorAccidentallyTouchHelper {
    private static volatile ColorDummyAccidentallyTouchHelper sInstance = null;

    public static ColorDummyAccidentallyTouchHelper getInstance() {
        if (sInstance == null) {
            synchronized (ColorDummyAccidentallyTouchHelper.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyAccidentallyTouchHelper();
                }
            }
        }
        return sInstance;
    }

    @Override // android.view.IColorAccidentallyTouchHelper
    public void initOnAmsReady() {
    }

    @Override // android.view.IColorAccidentallyTouchHelper
    public ColorAccidentallyTouchData getAccidentallyTouchData() {
        return new ColorAccidentallyTouchData();
    }

    @Override // android.view.IColorAccidentallyTouchHelper
    public MotionEvent updatePointerEvent(MotionEvent event, View mView, Configuration mLastConfiguration) {
        return event;
    }

    @Override // android.view.IColorAccidentallyTouchHelper
    public void updataeAccidentPreventionState(Context context, boolean enable, int rotation) {
    }

    @Override // android.view.IColorAccidentallyTouchHelper
    public void initData(Context context) {
    }

    @Override // android.view.IColorAccidentallyTouchHelper
    public boolean getEdgeEnable() {
        return false;
    }

    @Override // android.view.IColorAccidentallyTouchHelper
    public int getEdgeT1() {
        return 10;
    }

    @Override // android.view.IColorAccidentallyTouchHelper
    public int getEdgeT2() {
        return 30;
    }

    @Override // android.view.IColorAccidentallyTouchHelper
    public int getOriEdgeT1() {
        return 10;
    }
}
