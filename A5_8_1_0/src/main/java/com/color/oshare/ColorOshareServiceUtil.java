package com.color.oshare;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.provider.Settings.System;
import android.util.Log;
import com.color.oshare.IColorOshareService.Stub;

public class ColorOshareServiceUtil extends Stub {
    public static final String ACTION_OSHARE_STATE = "coloros.intent.action.OSHARE_STATE";
    private static final String KEY_SECURITY_CHECK_AGAIN = "key_security_check_again";
    public static final int OSHARE_OFF = 0;
    public static final int OSHARE_ON = 1;
    public static final String OSHARE_STATE = "oshare_state";
    private static final String PREFERENCE_PACKAGE = "com.coloros.oshare";
    private static final String SHARED_PREFERENCES_NAME = "oshare_preferences";
    protected static final String TAG = "OShareServiceUtil";
    private Context mContext;
    private DeathRecipient mDeathRecipient = new DeathRecipient() {
        public void binderDied() {
            ColorOshareServiceUtil.this.initShareEngine();
        }
    };
    private IColorOshareInitListener mInitListener;
    private IColorOshareCallback mOShareCallback;
    private IColorOshareService mService;
    private volatile boolean mServiceConnected = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(ColorOshareServiceUtil.TAG, "onServiceConnected");
            ColorOshareServiceUtil.this.mServiceConnected = true;
            ColorOshareServiceUtil.this.mService = Stub.asInterface(service);
            try {
                if (ColorOshareServiceUtil.this.mInitListener != null) {
                    ColorOshareServiceUtil.this.mInitListener.onShareInit();
                }
                if (ColorOshareServiceUtil.this.mService != null) {
                    ColorOshareServiceUtil.this.mService.registerCallback(ColorOshareServiceUtil.this.mOShareCallback);
                    ColorOshareServiceUtil.this.mService.scan();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(ColorOshareServiceUtil.TAG, "onServiceDisconnected");
            ColorOshareServiceUtil.this.mServiceConnected = false;
            try {
                if (ColorOshareServiceUtil.this.mInitListener != null) {
                    ColorOshareServiceUtil.this.mInitListener.onShareUninit();
                }
                if (ColorOshareServiceUtil.this.mService != null) {
                    ColorOshareServiceUtil.this.mService.unregisterCallback(ColorOshareServiceUtil.this.mOShareCallback);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            ColorOshareServiceUtil.this.mService = null;
            ColorOshareServiceUtil.this.mInitListener = null;
        }
    };

    public ColorOshareServiceUtil(Context context, IColorOshareInitListener listener) {
        this.mContext = context;
        this.mInitListener = listener;
    }

    public void initShareEngine() {
        Log.d(TAG, "initShareEngine");
        Intent intent = new Intent("com.coloros.oshare.OShareClient");
        if (this.mServiceConnection != null && (this.mServiceConnected ^ 1) != 0) {
            intent.setPackage(PREFERENCE_PACKAGE);
            this.mContext.bindService(intent, this.mServiceConnection, 1);
        }
    }

    public void scan() {
        Log.d(TAG, "scan");
        if (this.mService != null) {
            try {
                this.mService.scan();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerCallback(IColorOshareCallback callback) {
        Log.d(TAG, "registerCallback");
        if (this.mService != null) {
            try {
                this.mService.registerCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unregisterCallback(IColorOshareCallback callback) {
        Log.d(TAG, "unregisterCallback");
        if (this.mService != null) {
            try {
                this.mService.unregisterCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendData(Intent intent, ColorOshareDevice target) {
        Log.d(TAG, "sendData");
        if (this.mService != null) {
            try {
                this.mService.sendData(intent, target);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelTask(ColorOshareDevice device) {
        Log.d(TAG, "cancelTask");
        if (this.mService != null) {
            try {
                this.mService.cancelTask(device);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        Log.d(TAG, "stop : mServiceConnected = " + this.mServiceConnected);
        if (this.mService != null) {
            try {
                this.mService.stop();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (this.mServiceConnection != null && this.mServiceConnected) {
            try {
                this.mContext.unbindService(this.mServiceConnection);
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
        }
    }

    public static boolean isOshareOn(Context context) {
        ContentResolver cr = context.getContentResolver();
        if (checkRuntimePermission(context) && System.getInt(cr, OSHARE_STATE, 0) == 1) {
            return true;
        }
        return false;
    }

    public static void switchOshare(Context context, boolean isOn) {
        ContentResolver cr = context.getContentResolver();
        if (isOn) {
            Intent intent = new Intent("coloros.intent.action.SECURITY_CHECK");
            intent.setPackage(PREFERENCE_PACKAGE);
            try {
                context.startActivity(intent);
                return;
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }
        sendOshareStateBroadcast(context, isOn, cr);
    }

    private static void sendOshareStateBroadcast(Context context, boolean isOn, ContentResolver cr) {
        int i;
        int i2 = 1;
        Intent state = new Intent(ACTION_OSHARE_STATE);
        String str = OSHARE_STATE;
        if (isOn) {
            i = 1;
        } else {
            i = 0;
        }
        state.putExtra(str, i);
        context.sendBroadcast(state);
        String str2 = OSHARE_STATE;
        if (!isOn) {
            i2 = 0;
        }
        System.putInt(cr, str2, i2);
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, 7);
    }

    public static void setSecurityCheckAgain(Context context, boolean needCheckAgain) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_SECURITY_CHECK_AGAIN, needCheckAgain);
        editor.commit();
    }

    public static boolean isSecurityCheckAgain(Context context) {
        Context c = null;
        try {
            c = context.createPackageContext(PREFERENCE_PACKAGE, 2);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (c != null) {
            return getSharedPreferences(c).getBoolean(KEY_SECURITY_CHECK_AGAIN, true);
        }
        return true;
    }

    public boolean isSendOn() {
        if (this.mService != null) {
            try {
                return this.mService.isSendOn();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void switchSend(boolean isOn) {
        if (this.mService != null) {
            try {
                this.mService.switchSend(isOn);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void pause() throws RemoteException {
        Log.d(TAG, "pause : mServiceConnected = " + this.mServiceConnected);
        if (this.mService != null) {
            try {
                this.mService.pause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void resume() throws RemoteException {
        Log.d(TAG, "resume : mServiceConnected = " + this.mServiceConnected);
        if (this.mService != null) {
            try {
                this.mService.resume();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkRuntimePermission(Context context) {
        boolean checkSelfPermissionResult = true;
        if (context.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            checkSelfPermissionResult = false;
        }
        if (context.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
            checkSelfPermissionResult = false;
        }
        if (context.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") != 0) {
            checkSelfPermissionResult = false;
        }
        if (context.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != 0) {
            return false;
        }
        return checkSelfPermissionResult;
    }
}
