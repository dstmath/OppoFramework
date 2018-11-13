package android.media;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.media.MediaMetadata.Builder;
import android.media.session.MediaSession;
import android.media.session.MediaSession.Callback;
import android.media.session.MediaSessionLegacyHelper;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@Deprecated
public class RemoteControlClient {
    private static final boolean DEBUG = false;
    public static final int DEFAULT_PLAYBACK_VOLUME = 15;
    public static final int DEFAULT_PLAYBACK_VOLUME_HANDLING = 1;
    public static final int FLAGS_KEY_MEDIA_NONE = 0;
    public static final int FLAG_INFORMATION_REQUEST_ALBUM_ART = 8;
    public static final int FLAG_INFORMATION_REQUEST_KEY_MEDIA = 2;
    public static final int FLAG_INFORMATION_REQUEST_METADATA = 1;
    public static final int FLAG_INFORMATION_REQUEST_PLAYSTATE = 4;
    public static final int FLAG_KEY_MEDIA_FAST_FORWARD = 64;
    public static final int FLAG_KEY_MEDIA_NEXT = 128;
    public static final int FLAG_KEY_MEDIA_PAUSE = 16;
    public static final int FLAG_KEY_MEDIA_PLAY = 4;
    public static final int FLAG_KEY_MEDIA_PLAY_PAUSE = 8;
    public static final int FLAG_KEY_MEDIA_POSITION_UPDATE = 256;
    public static final int FLAG_KEY_MEDIA_PREVIOUS = 1;
    public static final int FLAG_KEY_MEDIA_RATING = 512;
    public static final int FLAG_KEY_MEDIA_REWIND = 2;
    public static final int FLAG_KEY_MEDIA_STOP = 32;
    public static int MEDIA_POSITION_READABLE = 0;
    public static int MEDIA_POSITION_WRITABLE = 0;
    public static final int PLAYBACKINFO_INVALID_VALUE = Integer.MIN_VALUE;
    public static final int PLAYBACKINFO_PLAYBACK_TYPE = 1;
    public static final int PLAYBACKINFO_USES_STREAM = 5;
    public static final int PLAYBACKINFO_VOLUME = 2;
    public static final int PLAYBACKINFO_VOLUME_HANDLING = 4;
    public static final int PLAYBACKINFO_VOLUME_MAX = 3;
    public static final long PLAYBACK_POSITION_ALWAYS_UNKNOWN = -9216204211029966080L;
    public static final long PLAYBACK_POSITION_INVALID = -1;
    public static final float PLAYBACK_SPEED_1X = 1.0f;
    public static final int PLAYBACK_TYPE_LOCAL = 0;
    private static final int PLAYBACK_TYPE_MAX = 1;
    private static final int PLAYBACK_TYPE_MIN = 0;
    public static final int PLAYBACK_TYPE_REMOTE = 1;
    public static final int PLAYBACK_VOLUME_FIXED = 0;
    public static final int PLAYBACK_VOLUME_VARIABLE = 1;
    public static final int PLAYSTATE_BUFFERING = 8;
    public static final int PLAYSTATE_ERROR = 9;
    public static final int PLAYSTATE_FAST_FORWARDING = 4;
    public static final int PLAYSTATE_NONE = 0;
    public static final int PLAYSTATE_PAUSED = 2;
    public static final int PLAYSTATE_PLAYING = 3;
    public static final int PLAYSTATE_REWINDING = 5;
    public static final int PLAYSTATE_SKIPPING_BACKWARDS = 7;
    public static final int PLAYSTATE_SKIPPING_FORWARDS = 6;
    public static final int PLAYSTATE_STOPPED = 1;
    private static final long POSITION_DRIFT_MAX_MS = 500;
    private static final long POSITION_REFRESH_PERIOD_MIN_MS = 2000;
    private static final long POSITION_REFRESH_PERIOD_PLAYING_MS = 15000;
    public static final int RCSE_ID_UNREGISTERED = -1;
    private static final String TAG = "RemoteControlClient";
    private final Object mCacheLock;
    private int mCurrentClientGenId;
    private MediaMetadata mMediaMetadata;
    private Bundle mMetadata;
    private OnMetadataUpdateListener mMetadataUpdateListener;
    private boolean mNeedsPositionSync;
    private Bitmap mOriginalArtwork;
    private long mPlaybackPositionMs;
    private float mPlaybackSpeed;
    private int mPlaybackState;
    private long mPlaybackStateChangeTimeMs;
    private OnGetPlaybackPositionListener mPositionProvider;
    private OnPlaybackPositionUpdateListener mPositionUpdateListener;
    private final PendingIntent mRcMediaIntent;
    private MediaSession mSession;
    private PlaybackState mSessionPlaybackState;
    private int mTransportControlFlags;
    private Callback mTransportListener;

    /* renamed from: android.media.RemoteControlClient$1 */
    class AnonymousClass1 extends Callback {
        final /* synthetic */ RemoteControlClient this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.media.RemoteControlClient.1.<init>(android.media.RemoteControlClient):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(android.media.RemoteControlClient r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.media.RemoteControlClient.1.<init>(android.media.RemoteControlClient):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteControlClient.1.<init>(android.media.RemoteControlClient):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.RemoteControlClient.1.onSeekTo(long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onSeekTo(long r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.RemoteControlClient.1.onSeekTo(long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteControlClient.1.onSeekTo(long):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.media.RemoteControlClient.1.onSetRating(android.media.Rating):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onSetRating(android.media.Rating r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.media.RemoteControlClient.1.onSetRating(android.media.Rating):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteControlClient.1.onSetRating(android.media.Rating):void");
        }
    }

    @Deprecated
    public class MetadataEditor extends MediaMetadataEditor {
        public static final int BITMAP_KEY_ARTWORK = 100;
        public static final int METADATA_KEY_ARTWORK = 100;
        final /* synthetic */ RemoteControlClient this$0;

        /* synthetic */ MetadataEditor(RemoteControlClient this$0, MetadataEditor metadataEditor) {
            this(this$0);
        }

        private MetadataEditor(RemoteControlClient this$0) {
            this.this$0 = this$0;
        }

        public Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }

        public /* bridge */ /* synthetic */ MediaMetadataEditor putString(int key, String value) throws IllegalArgumentException {
            return putString(key, value);
        }

        public synchronized MetadataEditor putString(int key, String value) throws IllegalArgumentException {
            super.putString(key, value);
            if (this.mMetadataBuilder != null) {
                String metadataKey = MediaMetadata.getKeyFromMetadataEditorKey(key);
                if (metadataKey != null) {
                    this.mMetadataBuilder.putText(metadataKey, value);
                }
            }
            return this;
        }

        public /* bridge */ /* synthetic */ MediaMetadataEditor putLong(int key, long value) throws IllegalArgumentException {
            return putLong(key, value);
        }

        public synchronized MetadataEditor putLong(int key, long value) throws IllegalArgumentException {
            super.putLong(key, value);
            if (this.mMetadataBuilder != null) {
                String metadataKey = MediaMetadata.getKeyFromMetadataEditorKey(key);
                if (metadataKey != null) {
                    this.mMetadataBuilder.putLong(metadataKey, value);
                }
            }
            return this;
        }

        public /* bridge */ /* synthetic */ MediaMetadataEditor putBitmap(int key, Bitmap bitmap) throws IllegalArgumentException {
            return putBitmap(key, bitmap);
        }

        public synchronized MetadataEditor putBitmap(int key, Bitmap bitmap) throws IllegalArgumentException {
            super.putBitmap(key, bitmap);
            if (this.mMetadataBuilder != null) {
                String metadataKey = MediaMetadata.getKeyFromMetadataEditorKey(key);
                if (metadataKey != null) {
                    this.mMetadataBuilder.putBitmap(metadataKey, bitmap);
                }
            }
            return this;
        }

        public /* bridge */ /* synthetic */ MediaMetadataEditor putObject(int key, Object object) throws IllegalArgumentException {
            return putObject(key, object);
        }

        public synchronized MetadataEditor putObject(int key, Object object) throws IllegalArgumentException {
            super.putObject(key, object);
            if (this.mMetadataBuilder != null && (key == MediaMetadataEditor.RATING_KEY_BY_USER || key == 101)) {
                String metadataKey = MediaMetadata.getKeyFromMetadataEditorKey(key);
                if (metadataKey != null) {
                    this.mMetadataBuilder.putRating(metadataKey, (Rating) object);
                }
            }
            return this;
        }

        public synchronized void clear() {
            super.clear();
        }

        public synchronized void apply() {
            if (this.mApplied) {
                Log.e(RemoteControlClient.TAG, "Can't apply a previously applied MetadataEditor");
                return;
            }
            synchronized (this.this$0.mCacheLock) {
                this.this$0.mMetadata = new Bundle(this.mEditorMetadata);
                this.this$0.mMetadata.putLong(String.valueOf(MediaMetadataEditor.KEY_EDITABLE_MASK), this.mEditableKeys);
                if (!(this.this$0.mOriginalArtwork == null || this.this$0.mOriginalArtwork.equals(this.mEditorArtwork))) {
                    this.this$0.mOriginalArtwork.recycle();
                }
                this.this$0.mOriginalArtwork = this.mEditorArtwork;
                this.mEditorArtwork = null;
                if (!(this.this$0.mSession == null || this.mMetadataBuilder == null)) {
                    this.this$0.mMediaMetadata = this.mMetadataBuilder.build();
                    this.this$0.mSession.setMetadata(this.this$0.mMediaMetadata);
                }
                this.mApplied = true;
            }
        }
    }

    public interface OnGetPlaybackPositionListener {
        long onGetPlaybackPosition();
    }

    public interface OnMetadataUpdateListener {
        void onMetadataUpdate(int i, Object obj);
    }

    public interface OnPlaybackPositionUpdateListener {
        void onPlaybackPositionUpdate(long j);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.media.RemoteControlClient.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.media.RemoteControlClient.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.RemoteControlClient.<clinit>():void");
    }

    public RemoteControlClient(PendingIntent mediaButtonIntent) {
        this.mCacheLock = new Object();
        this.mPlaybackState = 0;
        this.mPlaybackStateChangeTimeMs = 0;
        this.mPlaybackPositionMs = -1;
        this.mPlaybackSpeed = 1.0f;
        this.mTransportControlFlags = 0;
        this.mMetadata = new Bundle();
        this.mCurrentClientGenId = -1;
        this.mNeedsPositionSync = false;
        this.mSessionPlaybackState = null;
        this.mTransportListener = new AnonymousClass1(this);
        this.mRcMediaIntent = mediaButtonIntent;
    }

    public RemoteControlClient(PendingIntent mediaButtonIntent, Looper looper) {
        this.mCacheLock = new Object();
        this.mPlaybackState = 0;
        this.mPlaybackStateChangeTimeMs = 0;
        this.mPlaybackPositionMs = -1;
        this.mPlaybackSpeed = 1.0f;
        this.mTransportControlFlags = 0;
        this.mMetadata = new Bundle();
        this.mCurrentClientGenId = -1;
        this.mNeedsPositionSync = false;
        this.mSessionPlaybackState = null;
        this.mTransportListener = new AnonymousClass1(this);
        this.mRcMediaIntent = mediaButtonIntent;
    }

    public void registerWithSession(MediaSessionLegacyHelper helper) {
        helper.addRccListener(this.mRcMediaIntent, this.mTransportListener);
        this.mSession = helper.getSession(this.mRcMediaIntent);
        setTransportControlFlags(this.mTransportControlFlags);
    }

    public void unregisterWithSession(MediaSessionLegacyHelper helper) {
        helper.removeRccListener(this.mRcMediaIntent);
        this.mSession = null;
    }

    public MediaSession getMediaSession() {
        return this.mSession;
    }

    public MetadataEditor editMetadata(boolean startEmpty) {
        MetadataEditor editor = new MetadataEditor(this, null);
        if (startEmpty) {
            editor.mEditorMetadata = new Bundle();
            editor.mEditorArtwork = null;
            editor.mMetadataChanged = true;
            editor.mArtworkChanged = true;
            editor.mEditableKeys = 0;
        } else {
            editor.mEditorMetadata = new Bundle(this.mMetadata);
            editor.mEditorArtwork = this.mOriginalArtwork;
            editor.mMetadataChanged = false;
            editor.mArtworkChanged = false;
        }
        if (startEmpty || this.mMediaMetadata == null) {
            editor.mMetadataBuilder = new Builder();
        } else {
            editor.mMetadataBuilder = new Builder(this.mMediaMetadata);
        }
        return editor;
    }

    public void setPlaybackState(int state) {
        setPlaybackStateInt(state, PLAYBACK_POSITION_ALWAYS_UNKNOWN, 1.0f, false);
    }

    public void setPlaybackState(int state, long timeInMs, float playbackSpeed) {
        setPlaybackStateInt(state, timeInMs, playbackSpeed, true);
    }

    /* JADX WARNING: Missing block: B:25:0x0089, code:
            if (r8.mPlaybackSpeed != r12) goto L_0x0041;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setPlaybackStateInt(int state, long timeInMs, float playbackSpeed, boolean hasPosition) {
        if (DEBUG) {
            Log.d(TAG, "setPlaybackStateInt,state=:" + state + ",timeInMs=:" + timeInMs + ",hasPosition=:" + hasPosition);
        }
        synchronized (this.mCacheLock) {
            if (this.mPlaybackState == state && this.mPlaybackPositionMs == timeInMs) {
            }
            this.mPlaybackState = state;
            if (!hasPosition) {
                this.mPlaybackPositionMs = PLAYBACK_POSITION_ALWAYS_UNKNOWN;
            } else if (timeInMs < 0) {
                this.mPlaybackPositionMs = -1;
            } else {
                this.mPlaybackPositionMs = timeInMs;
            }
            this.mPlaybackSpeed = playbackSpeed;
            this.mPlaybackStateChangeTimeMs = SystemClock.elapsedRealtime();
            if (this.mSession != null) {
                long position;
                int pbState = PlaybackState.getStateFromRccState(state);
                if (hasPosition) {
                    position = this.mPlaybackPositionMs;
                } else {
                    position = -1;
                }
                PlaybackState.Builder bob = new PlaybackState.Builder(this.mSessionPlaybackState);
                bob.setState(pbState, position, playbackSpeed, SystemClock.elapsedRealtime());
                bob.setErrorMessage(null);
                this.mSessionPlaybackState = bob.build();
                this.mSession.setPlaybackState(this.mSessionPlaybackState);
            }
        }
    }

    public void setTransportControlFlags(int transportControlFlags) {
        synchronized (this.mCacheLock) {
            this.mTransportControlFlags = transportControlFlags;
            if (this.mSession != null) {
                PlaybackState.Builder bob = new PlaybackState.Builder(this.mSessionPlaybackState);
                bob.setActions(PlaybackState.getActionsFromRccControlFlags(transportControlFlags));
                this.mSessionPlaybackState = bob.build();
                this.mSession.setPlaybackState(this.mSessionPlaybackState);
            }
        }
    }

    public void setMetadataUpdateListener(OnMetadataUpdateListener l) {
        synchronized (this.mCacheLock) {
            this.mMetadataUpdateListener = l;
        }
    }

    public void setPlaybackPositionUpdateListener(OnPlaybackPositionUpdateListener l) {
        synchronized (this.mCacheLock) {
            this.mPositionUpdateListener = l;
        }
    }

    public void setOnGetPlaybackPositionListener(OnGetPlaybackPositionListener l) {
        synchronized (this.mCacheLock) {
            this.mPositionProvider = l;
        }
    }

    public PendingIntent getRcMediaIntent() {
        return this.mRcMediaIntent;
    }

    private void onSeekTo(int generationId, long timeMs) {
        synchronized (this.mCacheLock) {
            if (this.mCurrentClientGenId == generationId && this.mPositionUpdateListener != null) {
                this.mPositionUpdateListener.onPlaybackPositionUpdate(timeMs);
            }
        }
    }

    private void onUpdateMetadata(int generationId, int key, Object value) {
        synchronized (this.mCacheLock) {
            if (this.mCurrentClientGenId == generationId && this.mMetadataUpdateListener != null) {
                this.mMetadataUpdateListener.onMetadataUpdate(key, value);
            }
        }
    }

    static boolean playbackPositionShouldMove(int playstate) {
        switch (playstate) {
            case 1:
            case 2:
            case 6:
            case 7:
            case 8:
            case 9:
                return false;
            default:
                return true;
        }
    }

    private static long getCheckPeriodFromSpeed(float speed) {
        if (Math.abs(speed) <= 1.0f) {
            return POSITION_REFRESH_PERIOD_PLAYING_MS;
        }
        return Math.max((long) (15000.0f / Math.abs(speed)), POSITION_REFRESH_PERIOD_MIN_MS);
    }
}
