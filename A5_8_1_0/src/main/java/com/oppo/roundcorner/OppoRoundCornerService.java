package com.oppo.roundcorner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.oppo.roundcorner.IOppoRoundCornerService.Stub;

public class OppoRoundCornerService extends Stub {
    private static final String ACTION_CONFIGURATION_CHANGED = "android.intent.action.CONFIGURATION_CHANGED";
    public static final boolean DEBUG = true;
    public static final String TAG = "OPPORoundCorner";
    private BroadcastReceiver mConfigureChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("OPPORoundCorner", "onReceive, and action is: " + action);
            if (OppoRoundCornerService.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                int tempOrient = OppoRoundCornerWindowManager.getWindowManager(OppoRoundCornerService.this.mContext).getDefaultDisplay().getRotation();
                Log.d("OPPORoundCorner", "tempOrient= " + tempOrient + " mOrientation= " + OppoRoundCornerService.this.mOrientation);
                if (tempOrient != OppoRoundCornerService.this.mOrientation) {
                    OppoRoundCornerService.this.mOrientation = tempOrient;
                    OppoRoundCornerWindowManager.setOrientation(OppoRoundCornerService.this.mOrientation);
                    OppoRoundCornerWindowManager.updateLayout(OppoRoundCornerService.this.mContext);
                }
            }
        }
    };
    private Context mContext;
    private int mOrientation = 0;

    public OppoRoundCornerService(Context context) {
        this.mContext = context;
        OppoRoundCornerWindowManager.getScreenInfo(this.mContext);
    }

    public void init() {
        OppoRoundCornerWindowManager.createPortraitWindow(this.mContext);
        OppoRoundCornerWindowManager.createLandscapeWindow(this.mContext);
        OppoRoundCornerWindowManager.updateLayout(this.mContext);
        registerUsbStateReceiver();
    }

    private void registerUsbStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CONFIGURATION_CHANGED);
        this.mContext.registerReceiver(this.mConfigureChangeReceiver, intentFilter);
    }
}
