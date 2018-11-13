package android.telecom;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telecom.Phone.Listener;
import android.telecom.VideoProfile.CameraCapabilities;
import android.view.Surface;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IInCallAdapter;
import com.android.internal.telecom.IInCallService.Stub;
import com.mediatek.telecom.InCallServiceMessageAnalyzer;
import com.mediatek.telecom.TelecomTrace;
import java.util.Collections;
import java.util.List;

public abstract class InCallService extends Service {
    private static final int MSG_ADD_CALL = 2;
    private static final int MSG_BRING_TO_FOREGROUND = 6;
    private static final int MSG_ON_CALL_AUDIO_STATE_CHANGED = 5;
    private static final int MSG_ON_CAN_ADD_CALL_CHANGED = 7;
    private static final int MSG_ON_CONNECTION_EVENT = 9;
    private static final int MSG_SET_IN_CALL_ADAPTER = 1;
    private static final int MSG_SET_POST_DIAL_WAIT = 4;
    private static final int MSG_SILENCE_RINGER = 8;
    private static final int MSG_STORAGE_FULL = 11;
    private static final int MSG_UPDATE_CALL = 3;
    private static final int MSG_UPDATE_RECORD_STATE = 10;
    public static final String SERVICE_INTERFACE = "android.telecom.InCallService";
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            InCallService.this.mMessageAnalyzer.onMessageSent(msg);
            return super.sendMessageAtTime(msg, uptimeMillis);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            InCallService.this.mMessageAnalyzer.onStartHandleMessage(msg);
            if (InCallService.this.mPhone != null || msg.what == 1) {
                SomeArgs args;
                Phone -get2;
                switch (msg.what) {
                    case 1:
                        InCallService.this.mPhone = new Phone(new InCallAdapter((IInCallAdapter) msg.obj));
                        InCallService.this.mPhone.addListener(InCallService.this.mPhoneListener);
                        InCallService.this.onPhoneCreated(InCallService.this.mPhone);
                        break;
                    case 2:
                        InCallService.this.mPhone.internalAddCall((ParcelableCall) msg.obj);
                        break;
                    case 3:
                        TelecomTrace.begin("InCallService_update");
                        InCallService.this.mPhone.internalUpdateCall((ParcelableCall) msg.obj);
                        TelecomTrace.end("InCallService_update");
                        break;
                    case 4:
                        args = msg.obj;
                        try {
                            InCallService.this.mPhone.internalSetPostDialWait(args.arg1, args.arg2);
                            break;
                        } finally {
                            args.recycle();
                        }
                    case 5:
                        InCallService.this.mPhone.internalCallAudioStateChanged((CallAudioState) msg.obj);
                        break;
                    case 6:
                        -get2 = InCallService.this.mPhone;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        -get2.internalBringToForeground(z);
                        break;
                    case 7:
                        -get2 = InCallService.this.mPhone;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        -get2.internalSetCanAddCall(z);
                        break;
                    case 8:
                        InCallService.this.mPhone.internalSilenceRinger();
                        break;
                    case 9:
                        args = (SomeArgs) msg.obj;
                        try {
                            InCallService.this.mPhone.internalOnConnectionEvent((String) args.arg1, args.arg2, args.arg3);
                            break;
                        } finally {
                            args.recycle();
                        }
                    case 10:
                        args = (SomeArgs) msg.obj;
                        try {
                            if (InCallService.this.mPhone != null) {
                                InCallService.this.mPhone.internalUpdateRecordState(((Integer) args.arg1).intValue(), ((Integer) args.arg2).intValue());
                            }
                            args.recycle();
                            break;
                        } catch (Throwable th) {
                            args.recycle();
                        }
                    case 11:
                        InCallService.this.mPhone.internalOnStorageFull();
                        break;
                }
                InCallService.this.mMessageAnalyzer.onMessageHandled(msg);
                return;
            }
            InCallService.this.mMessageAnalyzer.onMessageHandled(msg);
        }
    };
    private InCallServiceMessageAnalyzer mMessageAnalyzer = new InCallServiceMessageAnalyzer();
    private Phone mPhone;
    private Listener mPhoneListener = new Listener() {
        public void onAudioStateChanged(Phone phone, AudioState audioState) {
            InCallService.this.onAudioStateChanged(audioState);
        }

        public void onCallAudioStateChanged(Phone phone, CallAudioState callAudioState) {
            InCallService.this.onCallAudioStateChanged(callAudioState);
        }

        public void onBringToForeground(Phone phone, boolean showDialpad) {
            InCallService.this.onBringToForeground(showDialpad);
        }

        public void onCallAdded(Phone phone, Call call) {
            InCallService.this.onCallAdded(call);
        }

        public void onCallRemoved(Phone phone, Call call) {
            InCallService.this.onCallRemoved(call);
        }

        public void onCanAddCallChanged(Phone phone, boolean canAddCall) {
            InCallService.this.onCanAddCallChanged(canAddCall);
        }

        public void onSilenceRinger(Phone phone) {
            InCallService.this.onSilenceRinger();
        }

        public void onUpdateRecordState(int state, int customValue) {
            InCallService.this.onUpdateRecordState(state, customValue);
        }

        public void onStorageFull() {
            InCallService.this.onStorageFull();
        }
    };

    private final class InCallServiceBinder extends Stub {
        /* synthetic */ InCallServiceBinder(InCallService this$0, InCallServiceBinder inCallServiceBinder) {
            this();
        }

        private InCallServiceBinder() {
        }

        public void setInCallAdapter(IInCallAdapter inCallAdapter) {
            InCallService.this.mHandler.obtainMessage(1, inCallAdapter).sendToTarget();
        }

        public void addCall(ParcelableCall call) {
            InCallService.this.mHandler.obtainMessage(2, call).sendToTarget();
        }

        public void updateCall(ParcelableCall call) {
            InCallService.this.mHandler.obtainMessage(3, call).sendToTarget();
        }

        public void setPostDial(String callId, String remaining) {
        }

        public void setPostDialWait(String callId, String remaining) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = remaining;
            InCallService.this.mHandler.obtainMessage(4, args).sendToTarget();
        }

        public void onCallAudioStateChanged(CallAudioState callAudioState) {
            InCallService.this.mHandler.obtainMessage(5, callAudioState).sendToTarget();
        }

        public void bringToForeground(boolean showDialpad) {
            int i;
            Handler -get0 = InCallService.this.mHandler;
            if (showDialpad) {
                i = 1;
            } else {
                i = 0;
            }
            -get0.obtainMessage(6, i, 0).sendToTarget();
        }

        public void onCanAddCallChanged(boolean canAddCall) {
            int i;
            Handler -get0 = InCallService.this.mHandler;
            if (canAddCall) {
                i = 1;
            } else {
                i = 0;
            }
            -get0.obtainMessage(7, i, 0).sendToTarget();
        }

        public void silenceRinger() {
            InCallService.this.mHandler.obtainMessage(8).sendToTarget();
        }

        public void onConnectionEvent(String callId, String event, Bundle extras) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = callId;
            args.arg2 = event;
            args.arg3 = extras;
            InCallService.this.mHandler.obtainMessage(9, args).sendToTarget();
        }

        public void updateRecordState(int state, int customValue) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(state);
            args.arg2 = Integer.valueOf(customValue);
            InCallService.this.mHandler.obtainMessage(10, args).sendToTarget();
        }

        public void onStorageFull() {
            InCallService.this.mHandler.obtainMessage(11).sendToTarget();
        }
    }

    public static abstract class VideoCall {

        public static abstract class Callback {
            public abstract void onCallDataUsageChanged(long j);

            public abstract void onCallSessionEvent(int i);

            public abstract void onCameraCapabilitiesChanged(CameraCapabilities cameraCapabilities);

            public abstract void onPeerDimensionsChanged(int i, int i2);

            public abstract void onPeerDimensionsWithAngleChanged(int i, int i2, int i3);

            public abstract void onSessionModifyRequestReceived(VideoProfile videoProfile);

            public abstract void onSessionModifyResponseReceived(int i, VideoProfile videoProfile, VideoProfile videoProfile2);

            public abstract void onVideoQualityChanged(int i);
        }

        public abstract void destroy();

        public abstract void registerCallback(Callback callback);

        public abstract void registerCallback(Callback callback, Handler handler);

        public abstract void requestCallDataUsage();

        public abstract void requestCameraCapabilities();

        public abstract void sendSessionModifyRequest(VideoProfile videoProfile);

        public abstract void sendSessionModifyResponse(VideoProfile videoProfile);

        public abstract void setCamera(String str);

        public abstract void setDeviceOrientation(int i);

        public abstract void setDisplaySurface(Surface surface);

        public abstract void setPauseImage(Uri uri);

        public abstract void setPreviewSurface(Surface surface);

        public abstract void setUIMode(int i);

        public abstract void setZoom(float f);

        public abstract void unregisterCallback(Callback callback);
    }

    public IBinder onBind(Intent intent) {
        return new InCallServiceBinder(this, null);
    }

    public boolean onUnbind(Intent intent) {
        if (this.mPhone != null) {
            Phone oldPhone = this.mPhone;
            this.mPhone = null;
            oldPhone.destroy();
            oldPhone.removeListener(this.mPhoneListener);
            onPhoneDestroyed(oldPhone);
        }
        return false;
    }

    @Deprecated
    public Phone getPhone() {
        return this.mPhone;
    }

    public final List<Call> getCalls() {
        return this.mPhone == null ? Collections.emptyList() : this.mPhone.getCalls();
    }

    public final boolean canAddCall() {
        return this.mPhone == null ? false : this.mPhone.canAddCall();
    }

    @Deprecated
    public final AudioState getAudioState() {
        return this.mPhone == null ? null : this.mPhone.getAudioState();
    }

    public final CallAudioState getCallAudioState() {
        return this.mPhone == null ? null : this.mPhone.getCallAudioState();
    }

    public final void setMuted(boolean state) {
        if (this.mPhone != null) {
            this.mPhone.setMuted(state);
        }
    }

    public final void setAudioRoute(int route) {
        if (this.mPhone != null) {
            this.mPhone.setAudioRoute(route);
        }
    }

    @Deprecated
    public void onPhoneCreated(Phone phone) {
    }

    @Deprecated
    public void onPhoneDestroyed(Phone phone) {
    }

    @Deprecated
    public void onAudioStateChanged(AudioState audioState) {
    }

    public void onCallAudioStateChanged(CallAudioState audioState) {
    }

    public void onBringToForeground(boolean showDialpad) {
    }

    public void onCallAdded(Call call) {
    }

    public void onCallRemoved(Call call) {
    }

    public void onCanAddCallChanged(boolean canAddCall) {
    }

    public void onSilenceRinger() {
    }

    public void onConnectionEvent(Call call, String event, Bundle extras) {
    }

    public void onUpdateRecordState(int state, int customValue) {
    }

    public void onStorageFull() {
    }

    public void startVoiceRecording() {
        if (this.mPhone != null) {
            this.mPhone.startVoiceRecording();
        }
    }

    public void stopVoiceRecording() {
        if (this.mPhone != null) {
            this.mPhone.stopVoiceRecording();
        }
    }

    public void setSortedIncomingCallList(List<String> incomingCallList) {
        if (this.mPhone != null) {
            this.mPhone.setSortedIncomingCallList(incomingCallList);
        }
    }

    public void explicitCallTransfer(String callId) {
        if (this.mPhone != null) {
            this.mPhone.explicitCallTransfer(callId);
        }
    }

    public void explicitCallTransfer(String callId, String number, int type) {
        if (this.mPhone != null) {
            this.mPhone.explicitCallTransfer(callId, number, type);
        }
    }

    public void hangupAll() {
        if (this.mPhone != null) {
            this.mPhone.hangupAll();
        }
    }

    public void hangupAllHoldCalls() {
        if (this.mPhone != null) {
            this.mPhone.hangupAllHoldCalls();
        }
    }

    public void hangupActiveAndAnswerWaiting() {
        if (this.mPhone != null) {
            this.mPhone.hangupActiveAndAnswerWaiting();
        }
    }
}
