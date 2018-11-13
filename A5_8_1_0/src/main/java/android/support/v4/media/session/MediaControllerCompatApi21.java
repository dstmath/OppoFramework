package android.support.v4.media.session;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession.Token;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.KeyEvent;

class MediaControllerCompatApi21 {

    public interface Callback {
        void onMetadataChanged(Object obj);

        void onPlaybackStateChanged(Object obj);

        void onSessionDestroyed();

        void onSessionEvent(String str, Bundle bundle);
    }

    static class CallbackProxy<T extends Callback> extends android.media.session.MediaController.Callback {
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
    }

    public static class PlaybackInfo {
        private static final int FLAG_SCO = 4;
        private static final int STREAM_BLUETOOTH_SCO = 6;
        private static final int STREAM_SYSTEM_ENFORCED = 7;

        public static int getPlaybackType(Object volumeInfoObj) {
            return ((android.media.session.MediaController.PlaybackInfo) volumeInfoObj).getPlaybackType();
        }

        public static AudioAttributes getAudioAttributes(Object volumeInfoObj) {
            return ((android.media.session.MediaController.PlaybackInfo) volumeInfoObj).getAudioAttributes();
        }

        public static int getLegacyAudioStream(Object volumeInfoObj) {
            return toLegacyStreamType(getAudioAttributes(volumeInfoObj));
        }

        public static int getVolumeControl(Object volumeInfoObj) {
            return ((android.media.session.MediaController.PlaybackInfo) volumeInfoObj).getVolumeControl();
        }

        public static int getMaxVolume(Object volumeInfoObj) {
            return ((android.media.session.MediaController.PlaybackInfo) volumeInfoObj).getMaxVolume();
        }

        public static int getCurrentVolume(Object volumeInfoObj) {
            return ((android.media.session.MediaController.PlaybackInfo) volumeInfoObj).getCurrentVolume();
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
                case 12:
                case 14:
                    return 3;
                case 2:
                    return 0;
                case 3:
                    return 8;
                case 4:
                    return 4;
                case 5:
                case 7:
                case 8:
                case 9:
                case 10:
                    return 5;
                case 6:
                    return 2;
                case 13:
                    return 1;
                default:
                    return 3;
            }
        }
    }

    public static class TransportControls {
        public static void play(Object controlsObj) {
            ((android.media.session.MediaController.TransportControls) controlsObj).play();
        }

        public static void pause(Object controlsObj) {
            ((android.media.session.MediaController.TransportControls) controlsObj).pause();
        }

        public static void stop(Object controlsObj) {
            ((android.media.session.MediaController.TransportControls) controlsObj).stop();
        }

        public static void seekTo(Object controlsObj, long pos) {
            ((android.media.session.MediaController.TransportControls) controlsObj).seekTo(pos);
        }

        public static void fastForward(Object controlsObj) {
            ((android.media.session.MediaController.TransportControls) controlsObj).fastForward();
        }

        public static void rewind(Object controlsObj) {
            ((android.media.session.MediaController.TransportControls) controlsObj).rewind();
        }

        public static void skipToNext(Object controlsObj) {
            ((android.media.session.MediaController.TransportControls) controlsObj).skipToNext();
        }

        public static void skipToPrevious(Object controlsObj) {
            ((android.media.session.MediaController.TransportControls) controlsObj).skipToPrevious();
        }

        public static void setRating(Object controlsObj, Object ratingObj) {
            ((android.media.session.MediaController.TransportControls) controlsObj).setRating((Rating) ratingObj);
        }
    }

    MediaControllerCompatApi21() {
    }

    public static Object fromToken(Context context, Object sessionToken) {
        return new MediaController(context, (Token) sessionToken);
    }

    public static Object createCallback(Callback callback) {
        return new CallbackProxy(callback);
    }

    public static void registerCallback(Object controllerObj, Object callbackObj, Handler handler) {
        ((MediaController) controllerObj).registerCallback((android.media.session.MediaController.Callback) callbackObj, handler);
    }

    public static void unregisterCallback(Object controllerObj, Object callbackObj) {
        ((MediaController) controllerObj).unregisterCallback((android.media.session.MediaController.Callback) callbackObj);
    }

    public static Object getTransportControls(Object controllerObj) {
        return ((MediaController) controllerObj).getTransportControls();
    }

    public static Object getPlaybackState(Object controllerObj) {
        return ((MediaController) controllerObj).getPlaybackState();
    }

    public static Object getMetadata(Object controllerObj) {
        return ((MediaController) controllerObj).getMetadata();
    }

    public static int getRatingType(Object controllerObj) {
        return ((MediaController) controllerObj).getRatingType();
    }

    public static Object getPlaybackInfo(Object controllerObj) {
        return ((MediaController) controllerObj).getPlaybackInfo();
    }

    public static boolean dispatchMediaButtonEvent(Object controllerObj, KeyEvent event) {
        return ((MediaController) controllerObj).dispatchMediaButtonEvent(event);
    }

    public static void sendCommand(Object controllerObj, String command, Bundle params, ResultReceiver cb) {
        ((MediaController) controllerObj).sendCommand(command, params, cb);
    }
}
