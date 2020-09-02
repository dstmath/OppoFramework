package com.android.server.display.ai.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.server.display.ai.MonotoneSplineManager;
import com.android.server.display.ai.bean.BrightnessPoint;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.coloros.deepthinker.brightness.TrainedBrightnessPoint;
import com.coloros.deepthinker.brightness.TrainedBrightnessResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainedReceiver extends BroadcastReceiver {
    private static final String TAG = "TrainedReceiver";
    private static volatile TrainedReceiver sInstance;
    private Context mContext;
    private boolean mIsRegistered = false;

    private TrainedReceiver() {
        ColorAILog.d(TAG, "Create TrainReceiver instance.");
    }

    public static TrainedReceiver getInstance() {
        if (sInstance == null) {
            synchronized (TrainedReceiver.class) {
                if (sInstance == null) {
                    sInstance = new TrainedReceiver();
                }
            }
        }
        return sInstance;
    }

    public void register(Context context) {
        this.mContext = context;
        if (!this.mIsRegistered) {
            context.getApplicationContext().registerReceiver(this, new IntentFilter(BrightnessConstants.ACTION_SPLINES_TRAINED), "oppo.permission.OPPO_COMPONENT_SAFE", null);
            this.mIsRegistered = true;
            ColorAILog.d(TAG, "Register TrainedReceiver.");
            return;
        }
        ColorAILog.d(TAG, "U have register TrainedReceiver.");
    }

    public void unregister() {
        ColorAILog.d(TAG, "Unregister receiver to avoid being registered multiple times.");
        try {
            this.mContext.getApplicationContext().unregisterReceiver(sInstance);
            this.mIsRegistered = false;
        } catch (Exception e) {
            ColorAILog.e(TAG, "Oops! Exception on unregister: " + e.getMessage());
        }
    }

    public void onReceive(final Context context, Intent intent) {
        if (intent != null && BrightnessConstants.ACTION_SPLINES_TRAINED.equals(intent.getAction())) {
            ColorAILog.d(TAG, "Train result is received.");
            if (!AIBrightnessTrainSwitch.getInstance().isTrainEnable()) {
                ColorAILog.d(TAG, "Train strategy is disabled. Do nothing.");
                return;
            }
            try {
                TrainedBrightnessResult result = intent.getParcelableExtra(BrightnessConstants.EXTRA_CENTRAL_POINTS);
                if (result != null) {
                    Map<String, List<TrainedBrightnessPoint>> trainedBrightnessPoints = result.getTrainedBrightnessPoints();
                    if (trainedBrightnessPoints != null) {
                        final Map<String, List<BrightnessPoint>> brightnessPoints = new HashMap<>();
                        for (Map.Entry<String, List<TrainedBrightnessPoint>> entry : trainedBrightnessPoints.entrySet()) {
                            String pkgName = entry.getKey();
                            List<BrightnessPoint> brightnessPointList = new ArrayList<>();
                            for (TrainedBrightnessPoint trainedBrightnessPoint : entry.getValue()) {
                                brightnessPointList.add(new BrightnessPoint(trainedBrightnessPoint.x, trainedBrightnessPoint.y));
                            }
                            brightnessPoints.put(pkgName, brightnessPointList);
                        }
                        try {
                            new Thread(new Runnable() {
                                /* class com.android.server.display.ai.broadcastreceiver.TrainedReceiver.AnonymousClass1 */

                                public void run() {
                                    MonotoneSplineManager.getInstance(context).updateSpline(context, brightnessPoints);
                                }
                            }).start();
                        } catch (Exception e) {
                            result = e;
                        }
                    }
                }
            } catch (Exception e2) {
                result = e2;
                ColorAILog.e(TAG, result.getMessage());
            }
        }
    }
}
