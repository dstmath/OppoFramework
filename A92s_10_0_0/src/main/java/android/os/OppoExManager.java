package android.os;

import android.util.Log;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.OppoWindowManager;

public final class OppoExManager {
    private static final String OPPO_EX_CHANNEL_NAME = "OppoExInputReceiver";
    public static final String SERVICE_NAME = "OPPOExService";
    public static final String TAG = "OppoExManager";
    private InputChannel mExInputChannel;
    private OppoExInputEventReceiver mExInputEventReceiver;
    OppoWindowManager mWindowManager;

    public interface IExInputEventReceiverCallback {
        boolean onInputEvent(InputEvent inputEvent);
    }

    public OppoExManager(IOppoExService service) {
        this.mExInputEventReceiver = null;
        this.mExInputChannel = new InputChannel();
        this.mWindowManager = new OppoWindowManager();
    }

    public OppoExManager() {
        this(null);
    }

    public boolean enableInputReceiver(Binder token, IExInputEventReceiverCallback callback) {
        if (this.mExInputEventReceiver != null) {
            return false;
        }
        try {
            this.mWindowManager.createMonitorInputConsumer(token, OPPO_EX_CHANNEL_NAME, this.mExInputChannel);
        } catch (RemoteException e) {
            Log.e(TAG, "enableInputReceiver e = " + e);
        }
        this.mExInputEventReceiver = new OppoExInputEventReceiver(this.mExInputChannel, Looper.getMainLooper());
        this.mExInputEventReceiver.setCallback(callback);
        return true;
    }

    public void disableInputReceiver() {
        OppoExInputEventReceiver oppoExInputEventReceiver = this.mExInputEventReceiver;
        if (oppoExInputEventReceiver != null) {
            oppoExInputEventReceiver.dispose();
            this.mExInputEventReceiver = null;
        }
        InputChannel inputChannel = this.mExInputChannel;
        if (inputChannel != null) {
            inputChannel.dispose();
            try {
                this.mWindowManager.destroyMonitorInputConsumer(OPPO_EX_CHANNEL_NAME);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private final class OppoExInputEventReceiver extends InputEventReceiver {
        private IExInputEventReceiverCallback mCallback;

        public void setCallback(IExInputEventReceiverCallback callback) {
            this.mCallback = callback;
        }

        public OppoExInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        @Override // android.view.InputEventReceiver
        public void onInputEvent(InputEvent event) {
            IExInputEventReceiverCallback iExInputEventReceiverCallback = this.mCallback;
            if (iExInputEventReceiverCallback != null) {
                finishInputEvent(event, iExInputEventReceiverCallback.onInputEvent(event));
            }
        }
    }
}
