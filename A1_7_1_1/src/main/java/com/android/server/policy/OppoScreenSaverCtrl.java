package com.android.server.policy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IOppoExInputCallBack.Stub;
import android.os.IOppoExService;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.service.dreams.DreamManagerInternal;
import android.service.dreams.IDreamManager;
import android.util.Slog;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.widget.Toast;
import com.android.server.LocalServices;
import com.android.server.oppo.IElsaManager;
import java.util.Calendar;

public class OppoScreenSaverCtrl {
    private static final boolean DEBUG = true;
    private static final String TAG = "OppoScreenSaverCtrl";
    private long CHECK_SETTING_TIME = 500;
    private long NO_INPUT_TIME = 30000;
    private int OFF_SCREEN_START_TIMEOUT = 15;
    private Context mContext;
    private IDreamManager mDreamManager = null;
    private DreamManagerInternal mDreamManagerInternal;
    private IOppoExService mExService = null;
    private final BroadcastReceiver mGetBatteryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                Slog.i(OppoScreenSaverCtrl.TAG, "mGetBatteryReceiver ACTION_BATTERY_CHANGED");
                Bundle bundle = intent.getExtras();
                int current = bundle.getInt("level");
                int total = bundle.getInt("scale");
                int status = bundle.getInt("status");
                OppoScreenSaverCtrl.this.mIsLowerBattery = false;
                if (((float) current) / ((float) total) < 0.2f && status != 2) {
                    if (OppoScreenSaverCtrl.this.mDreamManagerInternal == null) {
                        OppoScreenSaverCtrl.this.mDreamManagerInternal = (DreamManagerInternal) LocalServices.getService(DreamManagerInternal.class);
                    }
                    if (OppoScreenSaverCtrl.this.mDreamManagerInternal.isDreaming()) {
                        Slog.i(OppoScreenSaverCtrl.TAG, "mGetBatteryReceiver mDreamManagerInternal.isDreaming");
                        OppoScreenSaverCtrl.this.mDreamManagerInternal.stopDream(false);
                    }
                    Slog.i(OppoScreenSaverCtrl.TAG, "mGetBatteryReceiver showDisableDayDreamToast");
                    OppoScreenSaverCtrl.this.showDisableDayDreamToast(context);
                    OppoScreenSaverCtrl.this.mIsLowerBattery = true;
                }
            }
        }
    };
    private Handler mHandler;
    private Stub mInputReceiver = new Stub() {
        public void onInputEvent(InputEvent event) {
            if ((event instanceof MotionEvent) && (event.getSource() & 2) != 0 && ((MotionEvent) event).getAction() == 1) {
                OppoScreenSaverCtrl.this.mHandler.removeCallbacks(OppoScreenSaverCtrl.this.screenSaverRunnable);
                OppoScreenSaverCtrl.this.mHandler.postDelayed(OppoScreenSaverCtrl.this.screenSaverRunnable, OppoScreenSaverCtrl.this.NO_INPUT_TIME);
            }
        }
    };
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                OppoScreenSaverCtrl.this.mHandler.removeCallbacks(OppoScreenSaverCtrl.this.screenSaverRunnable);
                Slog.i(OppoScreenSaverCtrl.TAG, "mIntentReceiver screen on ");
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                OppoScreenSaverCtrl.this.mHandler.removeCallbacks(OppoScreenSaverCtrl.this.screenSaverRunnable);
                OppoScreenSaverCtrl.this.startDayDreamAlarm(context);
                Slog.i(OppoScreenSaverCtrl.TAG, "mIntentReceiver screen off ");
            }
        }
    };
    private boolean mIsLowerBattery = false;
    private StartDayDreamAlarm mStartDayDreamAlarm = new StartDayDreamAlarm(this, null);
    private final Runnable screenSaverRunnable = new Runnable() {
        public void run() {
            if (OppoScreenSaverCtrl.this.mIsLowerBattery) {
                Slog.i(OppoScreenSaverCtrl.TAG, "mGetBatteryReceiver private final Runnable screenSaverRunnable = new Runnable() {");
                return;
            }
            try {
                if (OppoScreenSaverCtrl.this.mDreamManager.isDreaming()) {
                    Slog.i(OppoScreenSaverCtrl.TAG, "screenSaverRunnable run() and mDreamManager is Dreaming");
                    OppoScreenSaverCtrl.this.mHandler.removeCallbacks(OppoScreenSaverCtrl.this.screenSaverRunnable);
                    return;
                }
                OppoScreenSaverCtrl.this.startDream();
            } catch (RemoteException e) {
                Slog.w(OppoScreenSaverCtrl.TAG, "Failed to dream", e);
            }
        }
    };
    private final Runnable startRunnable = new Runnable() {
        public void run() {
            Slog.i(OppoScreenSaverCtrl.TAG, "startRunnable run()");
            OppoScreenSaverCtrl.this.initDreamManager();
            OppoScreenSaverCtrl.this.startRun();
        }
    };

    private class StartDayDreamAlarm extends BroadcastReceiver {
        /* synthetic */ StartDayDreamAlarm(OppoScreenSaverCtrl this$0, StartDayDreamAlarm startDayDreamAlarm) {
            this();
        }

        private StartDayDreamAlarm() {
        }

        private void startDayDream(Context ctx) {
            if (OppoScreenSaverCtrl.this.mIsLowerBattery) {
                Slog.i(OppoScreenSaverCtrl.TAG, "mGetBatteryReceiver private class StartDayDreamAlarm extends BroadcastReceiver");
                return;
            }
            PowerManager pm = (PowerManager) ctx.getSystemService("power");
            IDreamManager mmDreamManager = IDreamManager.Stub.asInterface(ServiceManager.getService("dreams"));
            Slog.i(OppoScreenSaverCtrl.TAG, "startDayDream 222");
            if (!pm.isScreenOn()) {
                WakeLock wl = pm.newWakeLock(268435462, OppoScreenSaverCtrl.TAG);
                wl.acquire();
                try {
                    Slog.i(OppoScreenSaverCtrl.TAG, "startDayDream startDayDream");
                    ComponentName[] dreams = new ComponentName[]{new ComponentName("com.oppo.daydreamvideo", "com.oppo.daydreamvideo.DayDreamVideo")};
                    if (new ComponentName("com.oppo.daydreamvideo", "com.oppo.daydreamvideo.DayDreamVideo") == null) {
                        dreams = null;
                    }
                    mmDreamManager.setDreamComponents(dreams);
                    mmDreamManager.dream();
                } catch (RemoteException e) {
                    Slog.w(OppoScreenSaverCtrl.TAG, "Failed to dream", e);
                }
                wl.release();
            }
        }

        public void onReceive(Context context, Intent intent) {
            Slog.i(OppoScreenSaverCtrl.TAG, "StartDayDreamAlarm  onReceive");
            startDayDream(context);
        }
    }

    public OppoScreenSaverCtrl(Context context, Handler handler) {
        Slog.d(TAG, "new OppoScreenSaverCtrl");
        this.mHandler = handler;
        this.mContext = context;
        init();
    }

    private void init() {
        IntentFilter fsale = new IntentFilter();
        fsale.addAction("com.oppo.screensaver.on.screensaverctrl");
        this.mContext.registerReceiver(this.mStartDayDreamAlarm, fsale);
        IntentFilter fsale_GetBattery = new IntentFilter();
        fsale_GetBattery.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(this.mGetBatteryReceiver, fsale_GetBattery);
        Secure.putInt(this.mContext.getContentResolver(), "screensaver_enabled", 1);
        Secure.putInt(this.mContext.getContentResolver(), "screensaver_activate_on_dock", 1);
        Secure.putInt(this.mContext.getContentResolver(), "screensaver_activate_on_sleep", 1);
        this.mHandler.postDelayed(this.startRunnable, this.CHECK_SETTING_TIME);
    }

    private void initDreamManager() {
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.getService("dreams"));
        this.mDreamManagerInternal = (DreamManagerInternal) LocalServices.getService(DreamManagerInternal.class);
    }

    private void showDisableDayDreamToast(Context context) {
        Toast.makeText(context, "Low battery, Daydream is closed", 1).show();
    }

    private void startDayDreamAlarm(Context ctx) {
        PendingIntent sender = PendingIntent.getBroadcast(ctx, 0, new Intent("com.oppo.screensaver.on.screensaverctrl"), 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(13, this.OFF_SCREEN_START_TIMEOUT);
        ((AlarmManager) ctx.getSystemService("alarm")).setExact(0, calendar.getTimeInMillis(), sender);
        Slog.i(TAG, "startDayDream 111");
    }

    private void startDream() {
        try {
            if (this.mIsLowerBattery) {
                Slog.i(TAG, "mGetBatteryReceiver private void startDream()");
                return;
            }
            Slog.i(TAG, "screenSaverRunnable screen on start dream");
            ComponentName[] dreams = new ComponentName[]{new ComponentName("com.oppo.daydreamvideo", "com.oppo.daydreamvideo.DayDreamVideo")};
            IDreamManager iDreamManager = this.mDreamManager;
            if (new ComponentName("com.oppo.daydreamvideo", "com.oppo.daydreamvideo.DayDreamVideo") == null) {
                dreams = null;
            }
            iDreamManager.setDreamComponents(dreams);
            this.mDreamManager.dream();
        } catch (RemoteException e) {
            Slog.w(TAG, "Failed to dream", e);
        }
    }

    private void startRun() {
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        if (pm.isScreenOn()) {
            startDream();
        } else {
            WakeLock wl = pm.newWakeLock(268435462, TAG);
            wl.acquire();
            startDream();
            wl.release();
        }
        startInputCheck();
    }

    public boolean startInputCheck() {
        Slog.d(TAG, "startInputCheck " + Thread.currentThread().getId() + IElsaManager.EMPTY_PACKAGE);
        this.mExService = IOppoExService.Stub.asInterface(ServiceManager.getService("OPPOExService"));
        try {
            this.mExService.registerInputEvent(this.mInputReceiver);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            try {
                this.mContext.registerReceiver(this.mIntentReceiver, filter);
                return true;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e2) {
            Slog.w(TAG, "Failing registerInputEvent", e2);
            return false;
        }
    }

    public void stopinputCheck() {
        Slog.d(TAG, "stopinputCheck  ");
        if (this.mExService != null) {
            try {
                this.mExService.unregisterInputEvent(this.mInputReceiver);
            } catch (Exception e) {
                Slog.w(TAG, "Failing unregisterInputEvent", e);
            }
        }
        this.mContext.unregisterReceiver(this.mIntentReceiver);
        this.mContext.unregisterReceiver(this.mStartDayDreamAlarm);
        this.mContext.unregisterReceiver(this.mGetBatteryReceiver);
        this.mHandler.removeCallbacks(this.screenSaverRunnable);
    }
}
