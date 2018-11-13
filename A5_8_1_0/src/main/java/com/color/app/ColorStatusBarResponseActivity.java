package com.color.app;

import android.app.Activity;
import android.os.Bundle;
import com.color.app.ColorStatusBarResponseUtil.StatusBarClickListener;

public class ColorStatusBarResponseActivity extends Activity implements StatusBarClickListener {
    private static final String TAG = "ColorStatusBarResponseActivity";
    ColorStatusBarResponseUtil status;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.status = new ColorStatusBarResponseUtil(this);
        this.status.setStatusBarClickListener(this);
    }

    protected void onResume() {
        super.onResume();
        this.status.onResume();
    }

    protected void onPause() {
        super.onPause();
        this.status.onPause();
    }

    public void onStatusBarClicked() {
    }
}
