package android.support.v4.media.session;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes.Builder;
import android.media.MediaMetadata;
import android.media.Rating;
import android.media.VolumeProvider;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;

class MediaSessionCompatApi21 {

    public interface Callback {
        void onCommand(String str, Bundle bundle, ResultReceiver resultReceiver);

        void onFastForward();

        boolean onMediaButtonEvent(Intent intent);

        void onPause();

        void onPlay();

        void onRewind();

        void onSeekTo(long j);

        void onSetRating(Object obj);

        void onSkipToNext();

        void onSkipToPrevious();

        void onStop();
    }

    static class CallbackProxy<T extends Callback> extends android.media.session.MediaSession.Callback {
        protected final T mCallback;

        public CallbackProxy(T callback) {
            this.mCallback = callback;
        }

        public void onCommand(String command, Bundle args, ResultReceiver cb) {
            this.mCallback.onCommand(command, args, cb);
        }

        public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
            return this.mCallback.onMediaButtonEvent(mediaButtonIntent);
        }

        public void onPlay() {
            this.mCallback.onPlay();
        }

        public void onPause() {
            this.mCallback.onPause();
        }

        public void onSkipToNext() {
            this.mCallback.onSkipToNext();
        }

        public void onSkipToPrevious() {
            this.mCallback.onSkipToPrevious();
        }

        public void onFastForward() {
            this.mCallback.onFastForward();
        }

        public void onRewind() {
            this.mCallback.onRewind();
        }

        public void onStop() {
            this.mCallback.onStop();
        }

        public void onSeekTo(long pos) {
            this.mCallback.onSeekTo(pos);
        }

        public void onSetRating(Rating rating) {
            this.mCallback.onSetRating(rating);
        }
    }

    MediaSessionCompatApi21() {
    }

    public static Object createSession(Context context, String tag) {
        return new MediaSession(context, tag);
    }

    public static Object verifySession(Object mediaSession) {
        if (mediaSession instanceof MediaSession) {
            return mediaSession;
        }
        throw new IllegalArgumentException("mediaSession is not a valid MediaSession object");
    }

    public static Object createCallback(Callback callback) {
        return new CallbackProxy(callback);
    }

    public static void setCallback(Object sessionObj, Object callbackObj, Handler handler) {
        ((MediaSession) sessionObj).setCallback((android.media.session.MediaSession.Callback) callbackObj, handler);
    }

    public static void setFlags(Object sessionObj, int flags) {
        ((MediaSession) sessionObj).setFlags(flags);
    }

    public static void setPlaybackToLocal(Object sessionObj, int stream) {
        Builder bob = new Builder();
        bob.setLegacyStreamType(stream);
        ((MediaSession) sessionObj).setPlaybackToLocal(bob.build());
    }

    public static void setPlaybackToRemote(Object sessionObj, Object volumeProviderObj) {
        ((MediaSession) sessionObj).setPlaybackToRemote((VolumeProvider) volumeProviderObj);
    }

    public static void setActive(Object sessionObj, boolean active) {
        ((MediaSession) sessionObj).setActive(active);
    }

    public static boolean isActive(Object sessionObj) {
        return ((MediaSession) sessionObj).isActive();
    }

    public static void sendSessionEvent(Object sessionObj, String event, Bundle extras) {
        ((MediaSession) sessionObj).sendSessionEvent(event, extras);
    }

    public static void release(Object sessionObj) {
        ((MediaSession) sessionObj).release();
    }

    public static Parcelable getSessionToken(Object sessionObj) {
        return ((MediaSession) sessionObj).getSessionToken();
    }

    public static void setPlaybackState(Object sessionObj, Object stateObj) {
        ((MediaSession) sessionObj).setPlaybackState((PlaybackState) stateObj);
    }

    public static void setMetadata(Object sessionObj, Object metadataObj) {
        ((MediaSession) sessionObj).setMetadata((MediaMetadata) metadataObj);
    }
}
