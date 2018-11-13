package android.media;

import android.app.DownloadManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.media.AudioAttributes.Builder;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;

public class Ringtone {
    private static final boolean LOGD = true;
    private static final String[] MEDIA_COLUMNS = new String[]{DownloadManager.COLUMN_ID, "_data", "title"};
    private static final String MEDIA_SELECTION = "mime_type LIKE 'audio/%' OR mime_type IN ('application/ogg', 'application/x-flac')";
    private static final String TAG = "Ringtone";
    private static final ArrayList<Ringtone> sActiveRingtones = new ArrayList();
    private final boolean mAllowRemote;
    private AudioAttributes mAudioAttributes = new Builder().setUsage(6).setContentType(4).build();
    private final AudioManager mAudioManager;
    private final MyOnCompletionListener mCompletionListener = new MyOnCompletionListener();
    private final Context mContext;
    private boolean mIsLooping = false;
    private MediaPlayer mLocalPlayer;
    private final MyOnErrorListener mOnErrorListener = new MyOnErrorListener();
    private final Object mPlaybackSettingsLock = new Object();
    private final IRingtonePlayer mRemotePlayer;
    private final Binder mRemoteToken;
    private String mTitle;
    private Uri mUri;
    private float mVolume = 1.0f;

    class MyOnCompletionListener implements OnCompletionListener {
        MyOnCompletionListener() {
        }

        public void onCompletion(MediaPlayer mp) {
            synchronized (Ringtone.sActiveRingtones) {
                Ringtone.sActiveRingtones.remove(Ringtone.this);
            }
            mp.setOnCompletionListener(null);
            mp.setOnErrorListener(null);
        }
    }

    class MyOnErrorListener implements OnErrorListener {
        MyOnErrorListener() {
        }

        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.d(Ringtone.TAG, "mMediaPlayer OnError what:" + what + " extra:" + extra + " mp:" + mp);
            mp.setOnCompletionListener(null);
            mp.setOnErrorListener(null);
            Ringtone.this.destroyLocalPlayer();
            int callUid = Binder.getCallingUid();
            Log.d(Ringtone.TAG, "onError calling uid:" + callUid + " pid:" + Binder.getCallingPid());
            if (callUid >= 10000) {
                Log.w(Ringtone.TAG, "onError remote player disabled for 3rd apps");
                return true;
            }
            if (Ringtone.this.mAllowRemote && Ringtone.this.mRemotePlayer != null) {
                boolean looping;
                float volume;
                Uri canonicalUri = Ringtone.this.mUri.getCanonicalUri();
                synchronized (Ringtone.this.mPlaybackSettingsLock) {
                    looping = Ringtone.this.mIsLooping;
                    volume = Ringtone.this.mVolume;
                }
                try {
                    Ringtone.this.mRemotePlayer.play(Ringtone.this.mRemoteToken, canonicalUri, Ringtone.this.mAudioAttributes, volume, looping);
                } catch (RemoteException e) {
                    if (!Ringtone.this.playFallbackRingtone()) {
                        Log.w(Ringtone.TAG, "Problem playing ringtone:" + e);
                    }
                }
            }
            return true;
        }
    }

    public Ringtone(Context context, boolean allowRemote) {
        IRingtonePlayer ringtonePlayer;
        Binder binder = null;
        this.mContext = context;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mAllowRemote = allowRemote;
        if (allowRemote) {
            ringtonePlayer = this.mAudioManager.getRingtonePlayer();
        } else {
            ringtonePlayer = null;
        }
        this.mRemotePlayer = ringtonePlayer;
        if (allowRemote) {
            binder = new Binder();
        }
        this.mRemoteToken = binder;
    }

    @Deprecated
    public void setStreamType(int streamType) {
        PlayerBase.deprecateStreamTypeForPlayback(streamType, TAG, "setStreamType()");
        setAudioAttributes(new Builder().setInternalLegacyStreamType(streamType).build());
    }

    @Deprecated
    public int getStreamType() {
        return AudioAttributes.toLegacyStreamType(this.mAudioAttributes);
    }

    public void setAudioAttributes(AudioAttributes attributes) throws IllegalArgumentException {
        if (attributes == null) {
            throw new IllegalArgumentException("Invalid null AudioAttributes for Ringtone");
        }
        this.mAudioAttributes = attributes;
        setUri(this.mUri);
    }

    public AudioAttributes getAudioAttributes() {
        return this.mAudioAttributes;
    }

    public void setLooping(boolean looping) {
        synchronized (this.mPlaybackSettingsLock) {
            this.mIsLooping = looping;
            applyPlaybackProperties_sync();
        }
    }

    public void setVolume(float volume) {
        synchronized (this.mPlaybackSettingsLock) {
            if (volume < 0.0f) {
                volume = 0.0f;
            }
            if (volume > 1.0f) {
                volume = 1.0f;
            }
            this.mVolume = volume;
            applyPlaybackProperties_sync();
        }
    }

    private void applyPlaybackProperties_sync() {
        if (this.mLocalPlayer != null) {
            this.mLocalPlayer.setVolume(this.mVolume);
            this.mLocalPlayer.setLooping(this.mIsLooping);
        } else if (!this.mAllowRemote || this.mRemotePlayer == null) {
            Log.w(TAG, "Neither local nor remote player available when applying playback properties");
        } else {
            try {
                this.mRemotePlayer.setPlaybackProperties(this.mRemoteToken, this.mVolume, this.mIsLooping);
            } catch (RemoteException e) {
                Log.w(TAG, "Problem setting playback properties: ", e);
            }
        }
    }

    public String getTitle(Context context) {
        if (this.mTitle != null) {
            return this.mTitle;
        }
        String title = getTitle(context, this.mUri, true, this.mAllowRemote);
        this.mTitle = title;
        return title;
    }

    public static String getTitle(Context context, Uri uri, boolean followSettingsUri, boolean allowRemote) {
        ContentResolver res = context.getContentResolver();
        String title = null;
        if (uri != null) {
            String authority = ContentProvider.getAuthorityWithoutUserId(uri.getAuthority());
            if (!"settings".equals(authority)) {
                Cursor cursor = null;
                try {
                    if ("media".equals(authority)) {
                        cursor = res.query(uri, MEDIA_COLUMNS, allowRemote ? null : MEDIA_SELECTION, null, null);
                        if (cursor != null && cursor.getCount() == 1) {
                            cursor.moveToFirst();
                            String string = cursor.getString(2);
                            if (cursor != null) {
                                cursor.close();
                            }
                            return string;
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (SecurityException e) {
                    IRingtonePlayer mRemotePlayer = null;
                    if (allowRemote) {
                        mRemotePlayer = ((AudioManager) context.getSystemService("audio")).getRingtonePlayer();
                    }
                    if (mRemotePlayer != null) {
                        try {
                            title = mRemotePlayer.getTitle(uri);
                        } catch (RemoteException e2) {
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                if (title == null) {
                    title = uri.getLastPathSegment();
                }
            } else if (followSettingsUri) {
                Uri actualUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.getDefaultType(uri));
                title = context.getString(17040782, getTitle(context, actualUri, false, allowRemote));
            }
        } else {
            title = context.getString(17040786);
        }
        if (title == null) {
            title = context.getString(17040787);
            if (title == null) {
                title = "";
            }
        }
        return title;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x003e A:{Splitter: B:4:0x0011, ExcHandler: java.lang.SecurityException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x003e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:21:0x003f, code:
            destroyLocalPlayer();
     */
    /* JADX WARNING: Missing block: B:22:0x0044, code:
            if (r4.mAllowRemote == false) goto L_0x0046;
     */
    /* JADX WARNING: Missing block: B:23:0x0046, code:
            android.util.Log.w(TAG, "Remote playback not allowed: " + r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setUri(Uri uri) {
        destroyLocalPlayer();
        this.mUri = uri;
        if (this.mUri != null) {
            this.mLocalPlayer = new MediaPlayer();
            try {
                this.mLocalPlayer.setDataSource(this.mContext, this.mUri);
                this.mLocalPlayer.setAudioAttributes(this.mAudioAttributes);
                synchronized (this.mPlaybackSettingsLock) {
                    applyPlaybackProperties_sync();
                }
                this.mLocalPlayer.prepare();
            } catch (Exception e) {
            }
            if (this.mLocalPlayer != null) {
                Log.d(TAG, "Successfully created local player");
            } else {
                Log.d(TAG, "Problem opening; delegating to remote player");
            }
        }
    }

    public Uri getUri() {
        return this.mUri;
    }

    public void play() {
        if (this.mLocalPlayer != null) {
            this.mLocalPlayer.setOnErrorListener(this.mOnErrorListener);
            if (this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(this.mAudioAttributes)) != 0) {
                startLocalPlayer();
            }
        } else if (this.mAllowRemote && this.mRemotePlayer != null) {
            boolean looping;
            float volume;
            Uri canonicalUri = this.mUri.getCanonicalUri();
            synchronized (this.mPlaybackSettingsLock) {
                looping = this.mIsLooping;
                volume = this.mVolume;
            }
            try {
                this.mRemotePlayer.play(this.mRemoteToken, canonicalUri, this.mAudioAttributes, volume, looping);
            } catch (RemoteException e) {
                if (!playFallbackRingtone()) {
                    Log.w(TAG, "Problem playing ringtone: " + e);
                }
            }
        } else if (!playFallbackRingtone()) {
            Log.w(TAG, "Neither local nor remote playback available");
        }
    }

    public void stop() {
        if (this.mLocalPlayer != null) {
            destroyLocalPlayer();
        } else if (this.mAllowRemote && this.mRemotePlayer != null) {
            try {
                this.mRemotePlayer.stop(this.mRemoteToken);
            } catch (RemoteException e) {
                Log.w(TAG, "Problem stopping ringtone: " + e);
            }
        }
    }

    private void destroyLocalPlayer() {
        if (this.mLocalPlayer != null) {
            this.mLocalPlayer.setOnCompletionListener(null);
            this.mLocalPlayer.reset();
            this.mLocalPlayer.release();
            this.mLocalPlayer = null;
            synchronized (sActiveRingtones) {
                sActiveRingtones.remove(this);
            }
        }
    }

    private void startLocalPlayer() {
        if (this.mLocalPlayer != null) {
            synchronized (sActiveRingtones) {
                sActiveRingtones.add(this);
            }
            this.mLocalPlayer.setOnCompletionListener(this.mCompletionListener);
            this.mLocalPlayer.start();
        }
    }

    public boolean isPlaying() {
        if (this.mLocalPlayer != null) {
            return this.mLocalPlayer.isPlaying();
        }
        if (!this.mAllowRemote || this.mRemotePlayer == null) {
            Log.w(TAG, "Neither local nor remote playback available");
            return false;
        }
        try {
            return this.mRemotePlayer.isPlaying(this.mRemoteToken);
        } catch (RemoteException e) {
            Log.w(TAG, "Problem checking ringtone: " + e);
            return false;
        }
    }

    private boolean playFallbackRingtone() {
        if (this.mAudioManager.getStreamVolume(AudioAttributes.toLegacyStreamType(this.mAudioAttributes)) != 0) {
            int ringtoneType = RingtoneManager.getDefaultType(this.mUri);
            if (ringtoneType == -1 || RingtoneManager.getActualDefaultRingtoneUri(this.mContext, ringtoneType) != null) {
                try {
                    AssetFileDescriptor afd = this.mContext.getResources().openRawResourceFd(17825797);
                    if (afd != null) {
                        this.mLocalPlayer = new MediaPlayer();
                        if (afd.getDeclaredLength() < 0) {
                            this.mLocalPlayer.setDataSource(afd.getFileDescriptor());
                        } else {
                            this.mLocalPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
                        }
                        this.mLocalPlayer.setAudioAttributes(this.mAudioAttributes);
                        synchronized (this.mPlaybackSettingsLock) {
                            applyPlaybackProperties_sync();
                        }
                        this.mLocalPlayer.prepare();
                        startLocalPlayer();
                        afd.close();
                        return true;
                    }
                    Log.e(TAG, "Could not load fallback ringtone");
                } catch (IOException e) {
                    destroyLocalPlayer();
                    Log.e(TAG, "Failed to open fallback ringtone");
                } catch (NotFoundException e2) {
                    Log.e(TAG, "Fallback ringtone does not exist");
                }
            } else {
                Log.w(TAG, "not playing fallback for " + this.mUri);
            }
        }
        return false;
    }

    void setTitle(String title) {
        this.mTitle = title;
    }

    protected void finalize() {
        if (this.mLocalPlayer != null) {
            this.mLocalPlayer.release();
        }
    }
}
