package com.color.oshare;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import com.color.oshare.IColorOshareService;

public class ColorOshareServiceUtil extends IColorOshareService.Stub {
    public static final String ACTION_OSHARE_STATE = "coloros.intent.action.OSHARE_STATE";
    private static final String KEY_SECURITY_CHECK_AGAIN = "key_security_check_again";
    public static final int OSHARE_OFF = 0;
    public static final int OSHARE_ON = 1;
    public static final String OSHARE_STATE = "oshare_state";
    private static final String PREFERENCE_PACKAGE = "com.coloros.oshare";
    private static final String SHARED_PREFERENCES_NAME = "oshare_preferences";
    protected static final String TAG = "OShareServiceUtil";
    private Context mContext;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.color.oshare.ColorOshareServiceUtil.AnonymousClass1 */

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            ColorOshareServiceUtil.this.initShareEngine();
        }
    };
    /* access modifiers changed from: private */
    public IColorOshareInitListener mInitListener;
    /* access modifiers changed from: private */
    public IColorOshareCallback mOShareCallback;
    /* access modifiers changed from: private */
    public IColorOshareService mService;
    /* access modifiers changed from: private */
    public volatile boolean mServiceConnected = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.color.oshare.ColorOshareServiceUtil.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(ColorOshareServiceUtil.TAG, "onServiceConnected");
            boolean unused = ColorOshareServiceUtil.this.mServiceConnected = true;
            IColorOshareService unused2 = ColorOshareServiceUtil.this.mService = IColorOshareService.Stub.asInterface(service);
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

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.d(ColorOshareServiceUtil.TAG, "onServiceDisconnected");
            boolean unused = ColorOshareServiceUtil.this.mServiceConnected = false;
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
            IColorOshareService unused2 = ColorOshareServiceUtil.this.mService = null;
            IColorOshareInitListener unused3 = ColorOshareServiceUtil.this.mInitListener = null;
        }
    };

    public ColorOshareServiceUtil(Context context, IColorOshareInitListener listener) {
        this.mContext = context;
        this.mInitListener = listener;
    }

    public void initShareEngine() {
        Log.d(TAG, "initShareEngine");
        Intent intent = new Intent("com.coloros.oshare.OShareClient");
        if (this.mServiceConnection != null && !this.mServiceConnected) {
            intent.setPackage(PREFERENCE_PACKAGE);
            this.mContext.bindService(intent, this.mServiceConnection, 1);
        }
    }

    @Override // com.color.oshare.IColorOshareService
    public void scan() {
        Log.d(TAG, "scan");
        IColorOshareService iColorOshareService = this.mService;
        if (iColorOshareService != null) {
            try {
                iColorOshareService.scan();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override // com.color.oshare.IColorOshareService
    public void registerCallback(IColorOshareCallback callback) {
        Log.d(TAG, "registerCallback");
        IColorOshareService iColorOshareService = this.mService;
        if (iColorOshareService != null) {
            try {
                iColorOshareService.registerCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override // com.color.oshare.IColorOshareService
    public void unregisterCallback(IColorOshareCallback callback) {
        Log.d(TAG, "unregisterCallback");
        IColorOshareService iColorOshareService = this.mService;
        if (iColorOshareService != null) {
            try {
                iColorOshareService.unregisterCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override // com.color.oshare.IColorOshareService
    public void sendData(Intent intent, ColorOshareDevice target) {
        Log.d(TAG, "sendData");
        IColorOshareService iColorOshareService = this.mService;
        if (iColorOshareService != null) {
            try {
                iColorOshareService.sendData(intent, target);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override // com.color.oshare.IColorOshareService
    public void cancelTask(ColorOshareDevice device) {
        Log.d(TAG, "cancelTask");
        IColorOshareService iColorOshareService = this.mService;
        if (iColorOshareService != null) {
            try {
                iColorOshareService.cancelTask(device);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override // com.color.oshare.IColorOshareService
    public void stop() {
        Log.d(TAG, "stop : mServiceConnected = " + this.mServiceConnected);
        IColorOshareService iColorOshareService = this.mService;
        if (iColorOshareService != null) {
            try {
                iColorOshareService.stop();
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
        return checkRuntimePermission(context) && Settings.System.getInt(context.getContentResolver(), OSHARE_STATE, 0) == 1;
    }

    public static void switchOshare(Context context, boolean isOn) {
        ContentResolver cr = context.getContentResolver();
        if (isOn) {
            Intent intent = new Intent("coloros.intent.action.SECURITY_CHECK");
            intent.setPackage(PREFERENCE_PACKAGE);
            intent.addFlags(268435456);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            sendOshareStateBroadcast(context, isOn, cr);
        }
    }

    private static void sendOshareStateBroadcast(Context context, boolean isOn, ContentResolver cr) {
        Intent state = new Intent(ACTION_OSHARE_STATE);
        state.putExtra(OSHARE_STATE, isOn ? 1 : 0);
        context.sendBroadcast(state);
        Settings.System.putInt(cr, OSHARE_STATE, isOn);
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, 7);
    }

    public static void setSecurityCheckAgain(Context context, boolean needCheckAgain) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_SECURITY_CHECK_AGAIN, needCheckAgain);
        editor.commit();
    }

    public static boolean isSecurityCheckAgain(Context context) {
        Context c = null;
        try {
            c = context.createPackageContext(PREFERENCE_PACKAGE, 2);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (c != null) {
            return getSharedPreferences(c).getBoolean(KEY_SECURITY_CHECK_AGAIN, true);
        }
        return true;
    }

    @Override // com.color.oshare.IColorOshareService
    public boolean isSendOn() {
        IColorOshareService iColorOshareService = this.mService;
        if (iColorOshareService == null) {
            return false;
        }
        try {
            return iColorOshareService.isSendOn();
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override // com.color.oshare.IColorOshareService
    public void switchSend(boolean isOn) {
        IColorOshareService iColorOshareService = this.mService;
        if (iColorOshareService != null) {
            try {
                iColorOshareService.switchSend(isOn);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override // com.color.oshare.IColorOshareService
    public void pause() throws RemoteException {
        Log.d(TAG, "pause : mServiceConnected = " + this.mServiceConnected);
        IColorOshareService iColorOshareService = this.mService;
        if (iColorOshareService != null) {
            try {
                iColorOshareService.pause();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override // com.color.oshare.IColorOshareService
    public void resume() throws RemoteException {
        Log.d(TAG, "resume : mServiceConnected = " + this.mServiceConnected);
        IColorOshareService iColorOshareService = this.mService;
        if (iColorOshareService != null) {
            try {
                iColorOshareService.resume();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkRuntimePermission(Context context) {
        boolean checkSelfPermissionResult = true;
        if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != 0) {
            checkSelfPermissionResult = false;
        }
        if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != 0) {
            checkSelfPermissionResult = false;
        }
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != 0) {
            checkSelfPermissionResult = false;
        }
        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != 0) {
            return false;
        }
        return checkSelfPermissionResult;
    }
}
