package android.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import com.color.util.ColorAccidentallyTouchData;
import com.color.util.ColorAccidentallyTouchUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ColorAccidentallyTouchHelper implements IColorAccidentallyTouchHelper {
    static final String TAG_WM = "ColorAccidentallyManager";
    private static volatile ColorAccidentallyTouchHelper sInstance = null;

    public static ColorAccidentallyTouchHelper getInstance() {
        if (sInstance == null) {
            synchronized (ColorAccidentallyTouchHelper.class) {
                if (sInstance == null) {
                    sInstance = new ColorAccidentallyTouchHelper();
                }
            }
        }
        return sInstance;
    }

    public MotionEvent updatePointerEvent(MotionEvent event, View mView, Configuration mLastConfiguration) {
        return ColorAccidentallyTouchUtils.getInstance().updatePointerEvent(event, mView, mLastConfiguration);
    }

    public void initOnAmsReady() {
        ColorAccidentallyTouchUtils.getInstance().init();
    }

    public void initData(Context context) {
        ColorAccidentallyTouchUtils.getInstance().initData(context);
    }

    public ColorAccidentallyTouchData getAccidentallyTouchData() {
        return ColorAccidentallyTouchUtils.getInstance().getTouchData();
    }

    public void updataeAccidentPreventionState(Context context, boolean enable, final int rotation) {
        if (context.getPackageManager().hasSystemFeature("oppo.tp.limit.support")) {
            final String whiteList = enable ? "1" : "0";
            new Thread() {
                /* class android.view.ColorAccidentallyTouchHelper.AnonymousClass1 */

                public void run() {
                    int i = rotation;
                    if (i == 0) {
                        ColorAccidentallyTouchHelper.this.updateTouchPanelInfo("1", "0", whiteList);
                    } else if (i == 1) {
                        ColorAccidentallyTouchHelper.this.updateTouchPanelInfo("0", "1", whiteList);
                    } else if (i == 2) {
                        ColorAccidentallyTouchHelper.this.updateTouchPanelInfo("1", "0", whiteList);
                    } else if (i == 3) {
                        ColorAccidentallyTouchHelper.this.updateTouchPanelInfo("0", "2", whiteList);
                    }
                }
            }.start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTouchPanelInfo(String enable, String direct, String whiteList) {
        Log.d(TAG_WM, "accidentPrevention >>> updateTouchPanelInfo: " + whiteList + "," + direct + "," + enable);
        File listFile = new File("/proc/touchpanel/oppo_tp_limit_whitelist");
        File directFile = new File("/proc/touchpanel/oppo_tp_direction");
        File enableFile = new File("/proc/touchpanel/oppo_tp_limit_enable");
        writeTouchPanelFile(listFile, whiteList);
        writeTouchPanelFile(directFile, direct);
        writeTouchPanelFile(enableFile, enable);
    }

    private void writeTouchPanelFile(File file, String value) {
        if (!file.exists()) {
            Log.d(TAG_WM, "accidentPrevention >>> not exists: " + file);
            return;
        }
        Log.d(TAG_WM, "accidentPrevention >>> writeNarrowFile: " + file + ", " + value);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(value.getBytes());
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (out != null) {
                out.close();
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            if (out != null) {
                out.close();
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
    }

    public int getOriEdgeT1() {
        return ColorAccidentallyTouchUtils.getInstance().getOriEdgeT1();
    }

    public int getEdgeT2() {
        return ColorAccidentallyTouchUtils.getInstance().getEdgeT2();
    }

    public boolean getEdgeEnable() {
        return ColorAccidentallyTouchUtils.getInstance().getEdgeEnable();
    }

    public int getEdgeT1() {
        return ColorAccidentallyTouchUtils.getInstance().getEdgeT1();
    }
}
