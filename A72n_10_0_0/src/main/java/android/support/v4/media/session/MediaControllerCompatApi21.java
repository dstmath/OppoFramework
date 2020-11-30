package android.support.v4.media.session;

import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.ResultReceiver;
import com.alibaba.fastjson.parser.JSONToken;
import java.util.List;

class MediaControllerCompatApi21 {

    public interface Callback {
        void onAudioInfoChanged(int i, int i2, int i3, int i4, int i5);

        void onExtrasChanged(Bundle bundle);

        void onMetadataChanged(Object obj);

        void onPlaybackStateChanged(Object obj);

        void onQueueChanged(List<?> list);

        void onQueueTitleChanged(CharSequence charSequence);

        void onSessionDestroyed();

        void onSessionEvent(String str, Bundle bundle);
    }

    public static Object createCallback(Callback callback) {
        return new CallbackProxy(callback);
    }

    public static void unregisterCallback(Object controllerObj, Object callbackObj) {
        ((MediaController) controllerObj).unregisterCallback((MediaController.Callback) callbackObj);
    }

    public static void sendCommand(Object controllerObj, String command, Bundle params, ResultReceiver cb) {
        ((MediaController) controllerObj).sendCommand(command, params, cb);
    }

    public static class PlaybackInfo {
        public static AudioAttributes getAudioAttributes(Object volumeInfoObj) {
            return ((MediaController.PlaybackInfo) volumeInfoObj).getAudioAttributes();
        }

        public static int getLegacyAudioStream(Object volumeInfoObj) {
            return toLegacyStreamType(getAudioAttributes(volumeInfoObj));
        }

        private static int toLegacyStreamType(AudioAttributes aa) {
            if ((aa.getFlags() & 1) == 1) {
                return 7;
            }
            if ((aa.getFlags() & 4) == 4) {
                return 6;
            }
            switch (aa.getUsage()) {
                case 1:
                case 11:
                case JSONToken.LBRACE /* 12 */:
                case 14:
                    return 3;
                case 2:
                    return 0;
                case 3:
                    return 8;
                case 4:
                    return 4;
                case 5:
                case JSONToken.FALSE /* 7 */:
                case JSONToken.NULL /* 8 */:
                case 9:
                case 10:
                    return 5;
                case JSONToken.TRUE /* 6 */:
                    return 2;
                case JSONToken.RBRACE /* 13 */:
                    return 1;
                default:
                    return 3;
            }
        }
    }

    static class CallbackProxy<T extends Callback> extends MediaController.Callback {
        protected final T mCallback;

        public CallbackProxy(T callback) {
            this.mCallback = callback;
        }

        public void onSessionDestroyed() {
            this.mCallback.onSessionDestroyed();
        }

        public void onSessionEvent(String event, Bundle extras) {
            this.mCallback.onSessionEvent(event, extras);
        }

        public void onPlaybackStateChanged(PlaybackState state) {
            this.mCallback.onPlaybackStateChanged(state);
        }

        public void onMetadataChanged(MediaMetadata metadata) {
            this.mCallback.onMetadataChanged(metadata);
        }

        @Override // android.media.session.MediaController.Callback
        public void onQueueChanged(List<MediaSession.QueueItem> queue) {
            this.mCallback.onQueueChanged(queue);
        }

        public void onQueueTitleChanged(CharSequence title) {
            this.mCallback.onQueueTitleChanged(title);
        }

        public void onExtrasChanged(Bundle extras) {
            this.mCallback.onExtrasChanged(extras);
        }

        public void onAudioInfoChanged(MediaController.PlaybackInfo info) {
            this.mCallback.onAudioInfoChanged(info.getPlaybackType(), PlaybackInfo.getLegacyAudioStream(info), info.getVolumeControl(), info.getMaxVolume(), info.getCurrentVolume());
        }
    }
}
