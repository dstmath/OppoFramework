package android.support.v4.media.session;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat.Token;
import android.text.TextUtils;
import android.view.KeyEvent;

public final class MediaControllerCompat {
    private final MediaControllerImpl mImpl;

    public static abstract class Callback {
        final Object mCallbackObj;

        private class StubApi21 implements android.support.v4.media.session.MediaControllerCompatApi21.Callback {
            /* synthetic */ StubApi21(Callback this$1, StubApi21 stubApi21) {
                this();
            }

            private StubApi21() {
            }

            public void onSessionDestroyed() {
                Callback.this.onSessionDestroyed();
            }

            public void onSessionEvent(String event, Bundle extras) {
                Callback.this.onSessionEvent(event, extras);
            }

            public void onPlaybackStateChanged(Object stateObj) {
                Callback.this.onPlaybackStateChanged(PlaybackStateCompat.fromPlaybackState(stateObj));
            }

            public void onMetadataChanged(Object metadataObj) {
                Callback.this.onMetadataChanged(MediaMetadataCompat.fromMediaMetadata(metadataObj));
            }
        }

        public Callback() {
            if (VERSION.SDK_INT >= 21) {
                this.mCallbackObj = MediaControllerCompatApi21.createCallback(new StubApi21(this, null));
            } else {
                this.mCallbackObj = null;
            }
        }

        public void onSessionDestroyed() {
        }

        public void onSessionEvent(String event, Bundle extras) {
        }

        public void onPlaybackStateChanged(PlaybackStateCompat state) {
        }

        public void onMetadataChanged(MediaMetadataCompat metadata) {
        }
    }

    interface MediaControllerImpl {
        boolean dispatchMediaButtonEvent(KeyEvent keyEvent);

        Object getMediaController();

        MediaMetadataCompat getMetadata();

        PlaybackInfo getPlaybackInfo();

        PlaybackStateCompat getPlaybackState();

        int getRatingType();

        TransportControls getTransportControls();

        void registerCallback(Callback callback, Handler handler);

        void sendCommand(String str, Bundle bundle, ResultReceiver resultReceiver);

        void unregisterCallback(Callback callback);
    }

    static class MediaControllerImplApi21 implements MediaControllerImpl {
        private final Object mControllerObj;

        public MediaControllerImplApi21(Context context, MediaSessionCompat session) {
            this.mControllerObj = MediaControllerCompatApi21.fromToken(context, session.getSessionToken().getToken());
        }

        public MediaControllerImplApi21(Context context, Token sessionToken) throws RemoteException {
            this.mControllerObj = MediaControllerCompatApi21.fromToken(context, sessionToken.getToken());
            if (this.mControllerObj == null) {
                throw new RemoteException();
            }
        }

        public void registerCallback(Callback callback, Handler handler) {
            MediaControllerCompatApi21.registerCallback(this.mControllerObj, callback.mCallbackObj, handler);
        }

        public void unregisterCallback(Callback callback) {
            MediaControllerCompatApi21.unregisterCallback(this.mControllerObj, callback.mCallbackObj);
        }

        public boolean dispatchMediaButtonEvent(KeyEvent event) {
            return MediaControllerCompatApi21.dispatchMediaButtonEvent(this.mControllerObj, event);
        }

        public TransportControls getTransportControls() {
            Object controlsObj = MediaControllerCompatApi21.getTransportControls(this.mControllerObj);
            if (controlsObj != null) {
                return new TransportControlsApi21(controlsObj);
            }
            return null;
        }

        public PlaybackStateCompat getPlaybackState() {
            Object stateObj = MediaControllerCompatApi21.getPlaybackState(this.mControllerObj);
            if (stateObj != null) {
                return PlaybackStateCompat.fromPlaybackState(stateObj);
            }
            return null;
        }

        public MediaMetadataCompat getMetadata() {
            Object metadataObj = MediaControllerCompatApi21.getMetadata(this.mControllerObj);
            if (metadataObj != null) {
                return MediaMetadataCompat.fromMediaMetadata(metadataObj);
            }
            return null;
        }

        public int getRatingType() {
            return MediaControllerCompatApi21.getRatingType(this.mControllerObj);
        }

        public PlaybackInfo getPlaybackInfo() {
            Object volumeInfoObj = MediaControllerCompatApi21.getPlaybackInfo(this.mControllerObj);
            if (volumeInfoObj != null) {
                return new PlaybackInfo(android.support.v4.media.session.MediaControllerCompatApi21.PlaybackInfo.getPlaybackType(volumeInfoObj), android.support.v4.media.session.MediaControllerCompatApi21.PlaybackInfo.getLegacyAudioStream(volumeInfoObj), android.support.v4.media.session.MediaControllerCompatApi21.PlaybackInfo.getVolumeControl(volumeInfoObj), android.support.v4.media.session.MediaControllerCompatApi21.PlaybackInfo.getMaxVolume(volumeInfoObj), android.support.v4.media.session.MediaControllerCompatApi21.PlaybackInfo.getCurrentVolume(volumeInfoObj));
            }
            return null;
        }

        public void sendCommand(String command, Bundle params, ResultReceiver cb) {
            MediaControllerCompatApi21.sendCommand(this.mControllerObj, command, params, cb);
        }

        public Object getMediaController() {
            return this.mControllerObj;
        }
    }

    static class MediaControllerImplBase implements MediaControllerImpl {
        MediaControllerImplBase() {
        }

        public void registerCallback(Callback callback, Handler handler) {
        }

        public void unregisterCallback(Callback callback) {
        }

        public boolean dispatchMediaButtonEvent(KeyEvent event) {
            return false;
        }

        public TransportControls getTransportControls() {
            return null;
        }

        public PlaybackStateCompat getPlaybackState() {
            return null;
        }

        public MediaMetadataCompat getMetadata() {
            return null;
        }

        public int getRatingType() {
            return 0;
        }

        public PlaybackInfo getPlaybackInfo() {
            return null;
        }

        public void sendCommand(String command, Bundle params, ResultReceiver cb) {
        }

        public Object getMediaController() {
            return null;
        }
    }

    public static final class PlaybackInfo {
        public static final int PLAYBACK_TYPE_LOCAL = 1;
        public static final int PLAYBACK_TYPE_REMOTE = 2;
        private final int mAudioStream;
        private final int mCurrentVolume;
        private final int mMaxVolume;
        private final int mPlaybackType;
        private final int mVolumeControl;

        PlaybackInfo(int type, int stream, int control, int max, int current) {
            this.mPlaybackType = type;
            this.mAudioStream = stream;
            this.mVolumeControl = control;
            this.mMaxVolume = max;
            this.mCurrentVolume = current;
        }

        public int getPlaybackType() {
            return this.mPlaybackType;
        }

        public int getAudioStream() {
            return this.mAudioStream;
        }

        public int getVolumeControl() {
            return this.mVolumeControl;
        }

        public int getMaxVolume() {
            return this.mMaxVolume;
        }

        public int getCurrentVolume() {
            return this.mCurrentVolume;
        }
    }

    public static abstract class TransportControls {
        public abstract void fastForward();

        public abstract void pause();

        public abstract void play();

        public abstract void rewind();

        public abstract void seekTo(long j);

        public abstract void setRating(RatingCompat ratingCompat);

        public abstract void skipToNext();

        public abstract void skipToPrevious();

        public abstract void stop();

        TransportControls() {
        }
    }

    static class TransportControlsApi21 extends TransportControls {
        private final Object mControlsObj;

        public TransportControlsApi21(Object controlsObj) {
            this.mControlsObj = controlsObj;
        }

        public void play() {
            android.support.v4.media.session.MediaControllerCompatApi21.TransportControls.play(this.mControlsObj);
        }

        public void pause() {
            android.support.v4.media.session.MediaControllerCompatApi21.TransportControls.pause(this.mControlsObj);
        }

        public void stop() {
            android.support.v4.media.session.MediaControllerCompatApi21.TransportControls.stop(this.mControlsObj);
        }

        public void seekTo(long pos) {
            android.support.v4.media.session.MediaControllerCompatApi21.TransportControls.seekTo(this.mControlsObj, pos);
        }

        public void fastForward() {
            android.support.v4.media.session.MediaControllerCompatApi21.TransportControls.fastForward(this.mControlsObj);
        }

        public void rewind() {
            android.support.v4.media.session.MediaControllerCompatApi21.TransportControls.rewind(this.mControlsObj);
        }

        public void skipToNext() {
            android.support.v4.media.session.MediaControllerCompatApi21.TransportControls.skipToNext(this.mControlsObj);
        }

        public void skipToPrevious() {
            android.support.v4.media.session.MediaControllerCompatApi21.TransportControls.skipToPrevious(this.mControlsObj);
        }

        public void setRating(RatingCompat rating) {
            Object obj = null;
            Object obj2 = this.mControlsObj;
            if (rating != null) {
                obj = rating.getRating();
            }
            android.support.v4.media.session.MediaControllerCompatApi21.TransportControls.setRating(obj2, obj);
        }
    }

    public MediaControllerCompat(Context context, MediaSessionCompat session) {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        } else if (VERSION.SDK_INT >= 21) {
            this.mImpl = new MediaControllerImplApi21(context, session);
        } else {
            this.mImpl = new MediaControllerImplBase();
        }
    }

    public MediaControllerCompat(Context context, Token sessionToken) throws RemoteException {
        if (sessionToken == null) {
            throw new IllegalArgumentException("sessionToken must not be null");
        } else if (VERSION.SDK_INT >= 21) {
            this.mImpl = new MediaControllerImplApi21(context, sessionToken);
        } else {
            this.mImpl = new MediaControllerImplBase();
        }
    }

    public TransportControls getTransportControls() {
        return this.mImpl.getTransportControls();
    }

    public boolean dispatchMediaButtonEvent(KeyEvent keyEvent) {
        if (keyEvent != null) {
            return this.mImpl.dispatchMediaButtonEvent(keyEvent);
        }
        throw new IllegalArgumentException("KeyEvent may not be null");
    }

    public PlaybackStateCompat getPlaybackState() {
        return this.mImpl.getPlaybackState();
    }

    public MediaMetadataCompat getMetadata() {
        return this.mImpl.getMetadata();
    }

    public int getRatingType() {
        return this.mImpl.getRatingType();
    }

    public PlaybackInfo getPlaybackInfo() {
        return this.mImpl.getPlaybackInfo();
    }

    public void registerCallback(Callback callback) {
        registerCallback(callback, null);
    }

    public void registerCallback(Callback callback, Handler handler) {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
        if (handler == null) {
            handler = new Handler();
        }
        this.mImpl.registerCallback(callback, handler);
    }

    public void unregisterCallback(Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
        this.mImpl.unregisterCallback(callback);
    }

    public void sendCommand(String command, Bundle params, ResultReceiver cb) {
        if (TextUtils.isEmpty(command)) {
            throw new IllegalArgumentException("command cannot be null or empty");
        }
        this.mImpl.sendCommand(command, params, cb);
    }

    public Object getMediaController() {
        return this.mImpl.getMediaController();
    }
}
