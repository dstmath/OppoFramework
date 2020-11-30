package androidx.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import androidx.core.app.BundleCompat;
import androidx.media.MediaBrowser2;
import androidx.media.MediaController2;
import androidx.media.MediaSession2;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import java.util.List;
import java.util.concurrent.Executor;

@TargetApi(16)
class MediaController2ImplBase implements MediaController2.SupportLibraryImpl {
    private static final boolean DEBUG = Log.isLoggable("MC2ImplBase", 3);
    static final Bundle sDefaultRootExtras = new Bundle();
    private SessionCommandGroup2 mAllowedCommands;
    private MediaBrowserCompat mBrowserCompat;
    private int mBufferingState;
    private final MediaController2.ControllerCallback mCallback;
    private final Executor mCallbackExecutor;
    private volatile boolean mConnected;
    private final Context mContext;
    private MediaControllerCompat mControllerCompat;
    private ControllerCompatCallback mControllerCompatCallback;
    private MediaItem2 mCurrentMediaItem;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private MediaController2 mInstance;
    private boolean mIsReleased;
    private final Object mLock;
    private MediaMetadataCompat mMediaMetadataCompat;
    private MediaController2.PlaybackInfo mPlaybackInfo;
    private PlaybackStateCompat mPlaybackStateCompat;
    private int mPlayerState;
    private List<MediaItem2> mPlaylist;
    private MediaMetadata2 mPlaylistMetadata;
    private int mRepeatMode;
    private int mShuffleMode;
    private final SessionToken2 mToken;

    static {
        sDefaultRootExtras.putBoolean("androidx.media.root_default_root", true);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        boolean z = DEBUG;
        synchronized (this.mLock) {
            if (!this.mIsReleased) {
                this.mIsReleased = true;
                if (!(this.mControllerCompatCallback == null || this.mControllerCompatCallback.getIControllerCallback() == null)) {
                    cleanUpControllerCompatLocked(true);
                }
                if (this.mBrowserCompat != null) {
                    this.mBrowserCompat.disconnect();
                    this.mBrowserCompat = null;
                }
                this.mConnected = false;
                this.mCallbackExecutor.execute(new Runnable() {
                    /* class androidx.media.MediaController2ImplBase.AnonymousClass2 */

                    public void run() {
                        MediaController2ImplBase.this.mCallback.onDisconnected(MediaController2ImplBase.this.mInstance);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanUpControllerCompatLocked(boolean sendDisconnectCommand) {
        this.mHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= 18) {
            this.mHandlerThread.quitSafely();
        } else {
            this.mHandlerThread.quit();
        }
        if (sendDisconnectCommand) {
            sendCommand("androidx.media.controller.command.DISCONNECT");
        }
        synchronized (this.mLock) {
            if (this.mControllerCompat != null) {
                this.mControllerCompat.unregisterCallback(this.mControllerCompatCallback);
                this.mControllerCompat = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x009f, code lost:
        if (1 == 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00a1, code lost:
        close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00bd, code lost:
        r14.mCallbackExecutor.execute(new androidx.media.MediaController2ImplBase.AnonymousClass3(r14));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00c7, code lost:
        if (0 == 0) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00c9, code lost:
        close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        return;
     */
    public void onConnectedNotLocked(Bundle data) {
        data.setClassLoader(MediaSession2.class.getClassLoader());
        final SessionCommandGroup2 allowedCommands = SessionCommandGroup2.fromBundle(data.getBundle("androidx.media.argument.ALLOWED_COMMANDS"));
        int playerState = data.getInt("androidx.media.argument.PLAYER_STATE");
        int bufferingState = data.getInt("androidx.media.argument.BUFFERING_STATE");
        PlaybackStateCompat playbackStateCompat = (PlaybackStateCompat) data.getParcelable("androidx.media.argument.PLAYBACK_STATE_COMPAT");
        int repeatMode = data.getInt("androidx.media.argument.REPEAT_MODE");
        int shuffleMode = data.getInt("androidx.media.argument.SHUFFLE_MODE");
        List<MediaItem2> playlist = MediaUtils2.fromMediaItem2ParcelableArray(data.getParcelableArray("androidx.media.argument.PLAYLIST"));
        MediaItem2 currentMediaItem = MediaItem2.fromBundle(data.getBundle("androidx.media.argument.MEDIA_ITEM"));
        MediaController2.PlaybackInfo playbackInfo = MediaController2.PlaybackInfo.fromBundle(data.getBundle("androidx.media.argument.PLAYBACK_INFO"));
        MediaMetadata2 metadata = MediaMetadata2.fromBundle(data.getBundle("androidx.media.argument.PLAYLIST_METADATA"));
        if (DEBUG) {
            Log.d("MC2ImplBase", "onConnectedNotLocked sessionCompatToken=" + this.mToken.getSessionCompatToken() + ", allowedCommands=" + allowedCommands);
        }
        try {
            synchronized (this.mLock) {
                if (!this.mIsReleased) {
                    if (this.mConnected) {
                        Log.e("MC2ImplBase", "Cannot be notified about the connection result many times. Probably a bug or malicious app.");
                    } else {
                        this.mAllowedCommands = allowedCommands;
                        this.mPlayerState = playerState;
                        this.mBufferingState = bufferingState;
                        this.mPlaybackStateCompat = playbackStateCompat;
                        this.mRepeatMode = repeatMode;
                        this.mShuffleMode = shuffleMode;
                        this.mPlaylist = playlist;
                        this.mCurrentMediaItem = currentMediaItem;
                        this.mPlaylistMetadata = metadata;
                        this.mConnected = true;
                        this.mPlaybackInfo = playbackInfo;
                    }
                }
            }
        } finally {
            if (0 != 0) {
                close();
            }
        }
    }

    /* renamed from: androidx.media.MediaController2ImplBase$4  reason: invalid class name */
    class AnonymousClass4 extends ResultReceiver {
        final /* synthetic */ MediaController2ImplBase this$0;

        /* access modifiers changed from: protected */
        public void onReceiveResult(int resultCode, Bundle resultData) {
            if (this.this$0.mHandlerThread.isAlive()) {
                switch (resultCode) {
                    case JSONLexer.NOT_MATCH /* -1 */:
                        this.this$0.mCallbackExecutor.execute(new Runnable() {
                            /* class androidx.media.MediaController2ImplBase.AnonymousClass4.AnonymousClass1 */

                            public void run() {
                                AnonymousClass4.this.this$0.mCallback.onDisconnected(AnonymousClass4.this.this$0.mInstance);
                            }
                        });
                        this.this$0.close();
                        return;
                    case 0:
                        this.this$0.onConnectedNotLocked(resultData);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    private void sendCommand(String command) {
        sendCommand(command, null, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendCommand(String command, ResultReceiver receiver) {
        sendCommand(command, null, receiver);
    }

    private void sendCommand(String command, Bundle args, ResultReceiver receiver) {
        Throwable th;
        if (args == null) {
            args = new Bundle();
        }
        synchronized (this.mLock) {
            try {
                MediaControllerCompat controller = this.mControllerCompat;
                try {
                    BundleCompat.putBinder(args, "androidx.media.argument.ICONTROLLER_CALLBACK", this.mControllerCompatCallback.getIControllerCallback().asBinder());
                    args.putString("androidx.media.argument.PACKAGE_NAME", this.mContext.getPackageName());
                    args.putInt("androidx.media.argument.UID", Process.myUid());
                    args.putInt("androidx.media.argument.PID", Process.myPid());
                    controller.sendCommand(command, args, receiver);
                } catch (Throwable th2) {
                    th = th2;
                    while (true) {
                        try {
                            break;
                        } catch (Throwable th3) {
                            th = th3;
                        }
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ControllerCompatCallback extends MediaControllerCompat.Callback {
        final /* synthetic */ MediaController2ImplBase this$0;

        @Override // android.support.v4.media.session.MediaControllerCompat.Callback
        public void onSessionReady() {
            this.this$0.sendCommand("androidx.media.controller.command.CONNECT", new ResultReceiver(this.this$0.mHandler) {
                /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass1 */

                /* access modifiers changed from: protected */
                public void onReceiveResult(int resultCode, Bundle resultData) {
                    if (ControllerCompatCallback.this.this$0.mHandlerThread.isAlive()) {
                        switch (resultCode) {
                            case JSONLexer.NOT_MATCH /* -1 */:
                                synchronized (ControllerCompatCallback.this.this$0.mLock) {
                                    if (ControllerCompatCallback.this.this$0.mIsReleased) {
                                        ControllerCompatCallback.this.this$0.cleanUpControllerCompatLocked(false);
                                        return;
                                    }
                                    ControllerCompatCallback.this.this$0.mCallbackExecutor.execute(new Runnable() {
                                        /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass1.AnonymousClass1 */

                                        public void run() {
                                            ControllerCompatCallback.this.this$0.mCallback.onDisconnected(ControllerCompatCallback.this.this$0.mInstance);
                                        }
                                    });
                                    ControllerCompatCallback.this.this$0.close();
                                    return;
                                }
                            case 0:
                                synchronized (ControllerCompatCallback.this.this$0.mLock) {
                                    if (ControllerCompatCallback.this.this$0.mIsReleased) {
                                        ControllerCompatCallback.this.this$0.cleanUpControllerCompatLocked(true);
                                        return;
                                    } else {
                                        ControllerCompatCallback.this.this$0.onConnectedNotLocked(resultData);
                                        return;
                                    }
                                }
                            default:
                                return;
                        }
                    }
                }
            });
        }

        @Override // android.support.v4.media.session.MediaControllerCompat.Callback
        public void onSessionDestroyed() {
            this.this$0.close();
        }

        @Override // android.support.v4.media.session.MediaControllerCompat.Callback
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            synchronized (this.this$0.mLock) {
                this.this$0.mPlaybackStateCompat = state;
            }
        }

        @Override // android.support.v4.media.session.MediaControllerCompat.Callback
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            synchronized (this.this$0.mLock) {
                this.this$0.mMediaMetadataCompat = metadata;
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.support.v4.media.session.MediaControllerCompat.Callback
        public void onSessionEvent(String event, Bundle extras) {
            char c;
            if (extras != null) {
                extras.setClassLoader(MediaSession2.class.getClassLoader());
            }
            switch (event.hashCode()) {
                case -2076894204:
                    if (event.equals("androidx.media.session.event.ON_BUFFERING_STATE_CHANGED")) {
                        c = '\r';
                        break;
                    }
                    c = 65535;
                    break;
                case -2060536131:
                    if (event.equals("androidx.media.session.event.ON_PLAYBACK_SPEED_CHANGED")) {
                        c = '\f';
                        break;
                    }
                    c = 65535;
                    break;
                case -1588811870:
                    if (event.equals("androidx.media.session.event.ON_PLAYBACK_INFO_CHANGED")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case -1471144819:
                    if (event.equals("androidx.media.session.event.ON_PLAYER_STATE_CHANGED")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1021916189:
                    if (event.equals("androidx.media.session.event.ON_ERROR")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -617184370:
                    if (event.equals("androidx.media.session.event.ON_CURRENT_MEDIA_ITEM_CHANGED")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -92092013:
                    if (event.equals("androidx.media.session.event.ON_ROUTES_INFO_CHANGED")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -53555497:
                    if (event.equals("androidx.media.session.event.ON_REPEAT_MODE_CHANGED")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 229988025:
                    if (event.equals("androidx.media.session.event.SEND_CUSTOM_COMMAND")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 306321100:
                    if (event.equals("androidx.media.session.event.ON_PLAYLIST_METADATA_CHANGED")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 408969344:
                    if (event.equals("androidx.media.session.event.SET_CUSTOM_LAYOUT")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 806201420:
                    if (event.equals("androidx.media.session.event.ON_PLAYLIST_CHANGED")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 896576579:
                    if (event.equals("androidx.media.session.event.ON_SHUFFLE_MODE_CHANGED")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1398149092:
                    if (event.equals("androidx.media.session.event.ON_SEARCH_RESULT_CHANGED")) {
                        c = 16;
                        break;
                    }
                    c = 65535;
                    break;
                case 1696119769:
                    if (event.equals("androidx.media.session.event.ON_ALLOWED_COMMANDS_CHANGED")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1868367737:
                    if (event.equals("androidx.media.session.event.ON_CHILDREN_CHANGED")) {
                        c = 15;
                        break;
                    }
                    c = 65535;
                    break;
                case 1871849865:
                    if (event.equals("androidx.media.session.event.ON_SEEK_COMPLETED")) {
                        c = 14;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    final SessionCommandGroup2 allowedCommands = SessionCommandGroup2.fromBundle(extras.getBundle("androidx.media.argument.ALLOWED_COMMANDS"));
                    synchronized (this.this$0.mLock) {
                        this.this$0.mAllowedCommands = allowedCommands;
                    }
                    this.this$0.mCallbackExecutor.execute(new Runnable() {
                        /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass2 */

                        public void run() {
                            ControllerCompatCallback.this.this$0.mCallback.onAllowedCommandsChanged(ControllerCompatCallback.this.this$0.mInstance, allowedCommands);
                        }
                    });
                    return;
                case 1:
                    final int playerState = extras.getInt("androidx.media.argument.PLAYER_STATE");
                    PlaybackStateCompat state = (PlaybackStateCompat) extras.getParcelable("androidx.media.argument.PLAYBACK_STATE_COMPAT");
                    if (state != null) {
                        synchronized (this.this$0.mLock) {
                            this.this$0.mPlayerState = playerState;
                            this.this$0.mPlaybackStateCompat = state;
                        }
                        this.this$0.mCallbackExecutor.execute(new Runnable() {
                            /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass3 */

                            public void run() {
                                ControllerCompatCallback.this.this$0.mCallback.onPlayerStateChanged(ControllerCompatCallback.this.this$0.mInstance, playerState);
                            }
                        });
                        return;
                    }
                    return;
                case 2:
                    final MediaItem2 item = MediaItem2.fromBundle(extras.getBundle("androidx.media.argument.MEDIA_ITEM"));
                    synchronized (this.this$0.mLock) {
                        this.this$0.mCurrentMediaItem = item;
                    }
                    this.this$0.mCallbackExecutor.execute(new Runnable() {
                        /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass4 */

                        public void run() {
                            ControllerCompatCallback.this.this$0.mCallback.onCurrentMediaItemChanged(ControllerCompatCallback.this.this$0.mInstance, item);
                        }
                    });
                    return;
                case 3:
                    final int errorCode = extras.getInt("androidx.media.argument.ERROR_CODE");
                    final Bundle errorExtras = extras.getBundle("androidx.media.argument.EXTRAS");
                    this.this$0.mCallbackExecutor.execute(new Runnable() {
                        /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass5 */

                        public void run() {
                            ControllerCompatCallback.this.this$0.mCallback.onError(ControllerCompatCallback.this.this$0.mInstance, errorCode, errorExtras);
                        }
                    });
                    return;
                case 4:
                    final List<Bundle> routes = MediaUtils2.toBundleList(extras.getParcelableArray("androidx.media.argument.ROUTE_BUNDLE"));
                    this.this$0.mCallbackExecutor.execute(new Runnable() {
                        /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass6 */

                        public void run() {
                            ControllerCompatCallback.this.this$0.mCallback.onRoutesInfoChanged(ControllerCompatCallback.this.this$0.mInstance, routes);
                        }
                    });
                    return;
                case 5:
                    final MediaMetadata2 playlistMetadata = MediaMetadata2.fromBundle(extras.getBundle("androidx.media.argument.PLAYLIST_METADATA"));
                    final List<MediaItem2> playlist = MediaUtils2.fromMediaItem2ParcelableArray(extras.getParcelableArray("androidx.media.argument.PLAYLIST"));
                    synchronized (this.this$0.mLock) {
                        this.this$0.mPlaylist = playlist;
                        this.this$0.mPlaylistMetadata = playlistMetadata;
                    }
                    this.this$0.mCallbackExecutor.execute(new Runnable() {
                        /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass7 */

                        public void run() {
                            ControllerCompatCallback.this.this$0.mCallback.onPlaylistChanged(ControllerCompatCallback.this.this$0.mInstance, playlist, playlistMetadata);
                        }
                    });
                    return;
                case JSONToken.TRUE /* 6 */:
                    final MediaMetadata2 playlistMetadata2 = MediaMetadata2.fromBundle(extras.getBundle("androidx.media.argument.PLAYLIST_METADATA"));
                    synchronized (this.this$0.mLock) {
                        this.this$0.mPlaylistMetadata = playlistMetadata2;
                    }
                    this.this$0.mCallbackExecutor.execute(new Runnable() {
                        /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass8 */

                        public void run() {
                            ControllerCompatCallback.this.this$0.mCallback.onPlaylistMetadataChanged(ControllerCompatCallback.this.this$0.mInstance, playlistMetadata2);
                        }
                    });
                    return;
                case JSONToken.FALSE /* 7 */:
                    final int repeatMode = extras.getInt("androidx.media.argument.REPEAT_MODE");
                    synchronized (this.this$0.mLock) {
                        this.this$0.mRepeatMode = repeatMode;
                    }
                    this.this$0.mCallbackExecutor.execute(new Runnable() {
                        /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass9 */

                        public void run() {
                            ControllerCompatCallback.this.this$0.mCallback.onRepeatModeChanged(ControllerCompatCallback.this.this$0.mInstance, repeatMode);
                        }
                    });
                    return;
                case JSONToken.NULL /* 8 */:
                    final int shuffleMode = extras.getInt("androidx.media.argument.SHUFFLE_MODE");
                    synchronized (this.this$0.mLock) {
                        this.this$0.mShuffleMode = shuffleMode;
                    }
                    this.this$0.mCallbackExecutor.execute(new Runnable() {
                        /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass10 */

                        public void run() {
                            ControllerCompatCallback.this.this$0.mCallback.onShuffleModeChanged(ControllerCompatCallback.this.this$0.mInstance, shuffleMode);
                        }
                    });
                    return;
                case '\t':
                    Bundle commandBundle = extras.getBundle("androidx.media.argument.CUSTOM_COMMAND");
                    if (commandBundle != null) {
                        final SessionCommand2 command = SessionCommand2.fromBundle(commandBundle);
                        final Bundle args = extras.getBundle("androidx.media.argument.ARGUMENTS");
                        final ResultReceiver receiver = (ResultReceiver) extras.getParcelable("androidx.media.argument.RESULT_RECEIVER");
                        this.this$0.mCallbackExecutor.execute(new Runnable() {
                            /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass11 */

                            public void run() {
                                ControllerCompatCallback.this.this$0.mCallback.onCustomCommand(ControllerCompatCallback.this.this$0.mInstance, command, args, receiver);
                            }
                        });
                        return;
                    }
                    return;
                case '\n':
                    final List<MediaSession2.CommandButton> layout = MediaUtils2.fromCommandButtonParcelableArray(extras.getParcelableArray("androidx.media.argument.COMMAND_BUTTONS"));
                    if (layout != null) {
                        this.this$0.mCallbackExecutor.execute(new Runnable() {
                            /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass12 */

                            public void run() {
                                ControllerCompatCallback.this.this$0.mCallback.onCustomLayoutChanged(ControllerCompatCallback.this.this$0.mInstance, layout);
                            }
                        });
                        return;
                    }
                    return;
                case 11:
                    final MediaController2.PlaybackInfo info = MediaController2.PlaybackInfo.fromBundle(extras.getBundle("androidx.media.argument.PLAYBACK_INFO"));
                    if (info != null) {
                        synchronized (this.this$0.mLock) {
                            this.this$0.mPlaybackInfo = info;
                        }
                        this.this$0.mCallbackExecutor.execute(new Runnable() {
                            /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass13 */

                            public void run() {
                                ControllerCompatCallback.this.this$0.mCallback.onPlaybackInfoChanged(ControllerCompatCallback.this.this$0.mInstance, info);
                            }
                        });
                        return;
                    }
                    return;
                case JSONToken.LBRACE /* 12 */:
                    final PlaybackStateCompat state2 = (PlaybackStateCompat) extras.getParcelable("androidx.media.argument.PLAYBACK_STATE_COMPAT");
                    if (state2 != null) {
                        synchronized (this.this$0.mLock) {
                            this.this$0.mPlaybackStateCompat = state2;
                        }
                        this.this$0.mCallbackExecutor.execute(new Runnable() {
                            /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass14 */

                            public void run() {
                                ControllerCompatCallback.this.this$0.mCallback.onPlaybackSpeedChanged(ControllerCompatCallback.this.this$0.mInstance, state2.getPlaybackSpeed());
                            }
                        });
                        return;
                    }
                    return;
                case JSONToken.RBRACE /* 13 */:
                    final MediaItem2 item2 = MediaItem2.fromBundle(extras.getBundle("androidx.media.argument.MEDIA_ITEM"));
                    final int bufferingState = extras.getInt("androidx.media.argument.BUFFERING_STATE");
                    PlaybackStateCompat state3 = (PlaybackStateCompat) extras.getParcelable("androidx.media.argument.PLAYBACK_STATE_COMPAT");
                    if (item2 != null && state3 != null) {
                        synchronized (this.this$0.mLock) {
                            this.this$0.mBufferingState = bufferingState;
                            this.this$0.mPlaybackStateCompat = state3;
                        }
                        this.this$0.mCallbackExecutor.execute(new Runnable() {
                            /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass15 */

                            public void run() {
                                ControllerCompatCallback.this.this$0.mCallback.onBufferingStateChanged(ControllerCompatCallback.this.this$0.mInstance, item2, bufferingState);
                            }
                        });
                        return;
                    }
                    return;
                case 14:
                    final long position = extras.getLong("androidx.media.argument.SEEK_POSITION");
                    PlaybackStateCompat state4 = (PlaybackStateCompat) extras.getParcelable("androidx.media.argument.PLAYBACK_STATE_COMPAT");
                    if (state4 != null) {
                        synchronized (this.this$0.mLock) {
                            this.this$0.mPlaybackStateCompat = state4;
                        }
                        this.this$0.mCallbackExecutor.execute(new Runnable() {
                            /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass16 */

                            public void run() {
                                ControllerCompatCallback.this.this$0.mCallback.onSeekCompleted(ControllerCompatCallback.this.this$0.mInstance, position);
                            }
                        });
                        return;
                    }
                    return;
                case JSONToken.RBRACKET /* 15 */:
                    String parentId = extras.getString("androidx.media.argument.MEDIA_ID");
                    if (parentId != null && (this.this$0.mInstance instanceof MediaBrowser2)) {
                        ((MediaBrowser2.BrowserCallback) this.this$0.mCallback).onChildrenChanged((MediaBrowser2) this.this$0.mInstance, parentId, extras.getInt("androidx.media.argument.ITEM_COUNT", -1), extras.getBundle("androidx.media.argument.EXTRAS"));
                        return;
                    }
                    return;
                case 16:
                    final String query = extras.getString("androidx.media.argument.QUERY");
                    if (query != null && (this.this$0.mInstance instanceof MediaBrowser2)) {
                        final int itemCount = extras.getInt("androidx.media.argument.ITEM_COUNT", -1);
                        final Bundle searchExtras = extras.getBundle("androidx.media.argument.EXTRAS");
                        this.this$0.mCallbackExecutor.execute(new Runnable() {
                            /* class androidx.media.MediaController2ImplBase.ControllerCompatCallback.AnonymousClass17 */

                            public void run() {
                                ((MediaBrowser2.BrowserCallback) ControllerCompatCallback.this.this$0.mCallback).onSearchResultChanged((MediaBrowser2) ControllerCompatCallback.this.this$0.mInstance, query, itemCount, searchExtras);
                            }
                        });
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }
}
