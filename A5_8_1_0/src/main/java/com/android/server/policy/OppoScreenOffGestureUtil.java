package com.android.server.policy;

import android.content.Context;
import android.graphics.Point;
import android.os.SystemProperties;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class OppoScreenOffGestureUtil {
    private static int POINT_COUNT = 6;
    private static String PROC_PATH_COORDINATE = "/proc/touchpanel/coordinate";
    private static String SPLIT_POINT = ",";
    private static String SPLIT_X_Y = ":";
    private static String TAG = "OppoScreenOffGestureUtil";
    private boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    int mFlagClockwise = 1;
    int mGestureType = 0;
    Point[] mPoints = new Point[POINT_COUNT];

    OppoScreenOffGestureUtil(Context context) {
        for (int i = 0; i < this.mPoints.length; i++) {
            this.mPoints[i] = new Point();
        }
    }

    public void updateGestureInfo() {
        try {
            String[] strPoint = readFileFromProc(PROC_PATH_COORDINATE).split(SPLIT_POINT);
            this.mGestureType = Integer.parseInt(strPoint[0]);
            for (int i = 0; i < POINT_COUNT; i++) {
                String[] strXY = strPoint[i + 1].split(SPLIT_X_Y);
                this.mPoints[i].x = Integer.parseInt(strXY[0]);
                this.mPoints[i].y = Integer.parseInt(strXY[1]);
            }
            this.mFlagClockwise = Integer.parseInt(strPoint[strPoint.length - 1]);
            if (this.DEBUG) {
                for (int k = 0; k < this.mPoints.length; k++) {
                    Log.d(TAG, " ************* " + this.mPoints[k].x + "  " + this.mPoints[k].y + "  mFlagClockwise = " + this.mFlagClockwise + "  mGestureType = " + this.mGestureType);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "get gesture info error");
        }
    }

    public String readFileFromProc(String fileName) {
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String strline = br.readLine();
            fr.close();
            br.close();
            return strline;
        } catch (IOException e) {
            return "";
        }
    }
}
