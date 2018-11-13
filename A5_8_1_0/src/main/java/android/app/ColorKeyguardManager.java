package android.app;

import android.app.IColorKeyguardSessionCallback.Stub;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.IOppoWindowManagerImpl;

public class ColorKeyguardManager {
    private static String COMMAND_HEADER_SYNC = "Sync";
    private static final int MSG_COMMAND_ARRIVE = 1;
    private static final String TAG = "ColorKeyguardManager";
    private static boolean sIsRegistered;
    private static IKeyguardApp sKeyguardAppCallback;
    private static IOppoWindowManagerImpl sRemote;
    private KeyguardHandler mHandler;

    public interface IKeyguardApp {
        void onCommand(String str);

        void onSyncCommand(String str);
    }

    private class KeyguardHandler extends Handler {
        public KeyguardHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ColorKeyguardManager.this.handleCommand((String) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    static {
        sRemote = null;
        Log.i(TAG, "static init.");
        sRemote = new IOppoWindowManagerImpl();
    }

    public void registerKeyguardCallback(IKeyguardApp callback, String module) {
        sKeyguardAppCallback = callback;
        if (!sIsRegistered) {
            this.mHandler = new KeyguardHandler(Looper.getMainLooper());
            try {
                sIsRegistered = sRemote.openKeyguardSession(new Stub() {
                    public void onCommand(String command) {
                        Log.i(ColorKeyguardManager.TAG, "onCommand, command = " + command);
                        if (ColorKeyguardManager.this.isSyncCommand(command)) {
                            ColorKeyguardManager.this.scheduleArriveSyncCommand(command);
                        } else {
                            ColorKeyguardManager.this.scheduleArriveCommand(command);
                        }
                    }
                }, new Binder(), module);
            } catch (RemoteException e) {
                Log.w(TAG, "registerKeyguardCallback, ", e);
            }
        }
    }

    private boolean isSyncCommand(String command) {
        if (command == null) {
            return false;
        }
        String[] commands = new String[2];
        if (COMMAND_HEADER_SYNC.equals(command.split("-")[0])) {
            return true;
        }
        return false;
    }

    private void scheduleArriveSyncCommand(String command) {
        long start = System.currentTimeMillis();
        if (sKeyguardAppCallback != null) {
            sKeyguardAppCallback.onSyncCommand(command);
        }
        long spend = System.currentTimeMillis() - start;
        if (spend > 20) {
            Log.w(TAG, "Schedule sync command(" + command + ") spends " + spend + "ms, this may result in keyguard block.");
        }
    }

    private void scheduleArriveCommand(String command) {
        if (this.mHandler != null) {
            Message message = Message.obtain();
            message.what = 1;
            message.obj = command;
            this.mHandler.sendMessage(message);
        }
    }

    private void handleCommand(String command) {
        if (sKeyguardAppCallback != null) {
            sKeyguardAppCallback.onCommand(command);
        }
    }

    public void requestKeyguard(String command) {
        try {
            sRemote.requestKeyguard(command);
        } catch (RemoteException e) {
            Log.w(TAG, "requestKeyguard, ", e);
        }
    }
}
