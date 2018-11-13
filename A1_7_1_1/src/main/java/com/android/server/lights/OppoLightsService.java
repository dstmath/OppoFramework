package com.android.server.lights;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.System;
import android.util.Log;
import com.android.internal.os.BackgroundThread;

public class OppoLightsService extends LightsService {
    private static final boolean DEBUG = false;
    private static final String TAG = "OppoLightsService";
    BroadcastReceiver mCameraLightReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int nID = intent.getExtras().getInt("LightID");
            int nBrightness = intent.getExtras().getInt("LightBreightness");
            Light light = OppoLightsService.this.mService.getLight(nID);
            if (light != null) {
                light.setBrightness(nBrightness);
            }
        }
    };
    private Context mContext;

    public class ButtonLight extends LightImpl {
        public static final int BRIGHTNESS_DEFAULT = 100;
        private static final int MESSAGE_TURN_LIGHT_OFF = 1000;
        public static final int MODE_ALWAYS_OFF = 2;
        public static final int MODE_ALWAYS_ON = 1;
        public static final int MODE_AUTO_SENSOR = 4;
        public static final int MODE_AUTO_TIMEOUT = 3;
        public static final long TIMEOUT_DEFAULT = 6000;
        private int mBrightnessMode;
        private int mButtonLightMode;
        private long mButtonLightTimeout;
        private int mColor;
        private MyHandler mHandler;
        private boolean mHasEnabled;
        private int mMode;
        private int mOffMS;
        private int mOnMS;

        class MyHandler extends Handler {
            MyHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1000:
                        ButtonLight.this.turnButtonLightOff();
                        return;
                    default:
                        return;
                }
            }
        }

        class SettingsObserver extends ContentObserver {
            SettingsObserver(Handler handler) {
                super(handler);
            }

            void observe() {
                ContentResolver resolver = OppoLightsService.this.mContext.getContentResolver();
                resolver.registerContentObserver(System.getUriFor("button_light_mode"), false, this);
                resolver.registerContentObserver(System.getUriFor("button_light_timeout"), false, this);
                update();
            }

            public void onChange(boolean selfChange) {
                update();
            }

            public void update() {
                ContentResolver resolver = OppoLightsService.this.mContext.getContentResolver();
                ButtonLight.this.mButtonLightMode = System.getInt(resolver, "button_light_mode", 3);
                long buttonLightTimeout = System.getLong(resolver, "button_light_timeout", ButtonLight.TIMEOUT_DEFAULT);
                ButtonLight.this.mButtonLightTimeout = buttonLightTimeout;
                Log.d(OppoLightsService.TAG, "+++++++SettingsObserver update buttonLightTimeout=" + buttonLightTimeout);
                if (ButtonLight.this.mHasEnabled) {
                    synchronized (this) {
                        ButtonLight.this.setLightLocked(ButtonLight.this.mColor, ButtonLight.this.mMode, ButtonLight.this.mOnMS, ButtonLight.this.mOffMS, ButtonLight.this.mBrightnessMode);
                    }
                }
            }
        }

        /* synthetic */ ButtonLight(OppoLightsService this$0, int nId, ButtonLight buttonLight) {
            this(nId);
        }

        private ButtonLight(int nId) {
            super(nId);
            this.mColor = 0;
            this.mMode = 0;
            this.mOnMS = 0;
            this.mOffMS = 0;
            this.mBrightnessMode = 0;
            this.mHasEnabled = false;
            this.mHandler = new MyHandler(BackgroundThread.getHandler().getLooper());
            new SettingsObserver(this.mHandler).observe();
        }

        private void turnButtonLightOff() {
            synchronized (this) {
                super.setLightLocked(0, 0, 0, 0, 0);
            }
        }

        void setLightLocked(int color, int mode, int onMS, int offMS, int brightnessMode) {
            this.mHasEnabled = true;
            this.mHandler.removeMessages(1000);
            switch (this.mButtonLightMode) {
                case 1:
                    super.setLightLocked(color, mode, onMS, offMS, brightnessMode);
                    break;
                case 2:
                    super.setLightLocked(0, 0, 0, 0, 0);
                    break;
                case 3:
                    super.setLightLocked(color, mode, onMS, offMS, brightnessMode);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000), this.mButtonLightTimeout);
                    break;
                case 4:
                    break;
                default:
                    super.setLightLocked(color, mode, onMS, offMS, brightnessMode);
                    break;
            }
            this.mColor = color;
            this.mMode = mode;
            this.mOnMS = onMS;
            this.mOffMS = offMS;
            this.mBrightnessMode = brightnessMode;
        }
    }

    public OppoLightsService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void systemReady() {
        setLight(2, new ButtonLight(this, 2, null));
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.oppo.camera.OpenLight");
        this.mContext.registerReceiver(this.mCameraLightReceiver, filter, null, BackgroundThread.getHandler());
    }

    void setLight(int id, LightImpl light) {
        this.mLights[id] = light;
    }
}
