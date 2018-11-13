package android.support.v4.media.session;

import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.text.TextUtils;

public final class PlaybackStateCompat implements Parcelable {
    public static final long ACTION_FAST_FORWARD = 64;
    public static final long ACTION_PAUSE = 2;
    public static final long ACTION_PLAY = 4;
    public static final long ACTION_PLAY_FROM_MEDIA_ID = 1024;
    public static final long ACTION_PLAY_FROM_SEARCH = 2048;
    public static final long ACTION_PLAY_PAUSE = 512;
    public static final long ACTION_REWIND = 8;
    public static final long ACTION_SEEK_TO = 256;
    public static final long ACTION_SET_RATING = 128;
    public static final long ACTION_SKIP_TO_NEXT = 32;
    public static final long ACTION_SKIP_TO_PREVIOUS = 16;
    public static final long ACTION_SKIP_TO_QUEUE_ITEM = 4096;
    public static final long ACTION_STOP = 1;
    public static final Creator<PlaybackStateCompat> CREATOR = new Creator<PlaybackStateCompat>() {
        public PlaybackStateCompat createFromParcel(Parcel in) {
            return new PlaybackStateCompat(in, null);
        }

        public PlaybackStateCompat[] newArray(int size) {
            return new PlaybackStateCompat[size];
        }
    };
    public static final long PLAYBACK_POSITION_UNKNOWN = -1;
    public static final int STATE_BUFFERING = 6;
    public static final int STATE_CONNECTING = 8;
    public static final int STATE_ERROR = 7;
    public static final int STATE_FAST_FORWARDING = 4;
    public static final int STATE_NONE = 0;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_REWINDING = 5;
    public static final int STATE_SKIPPING_TO_NEXT = 10;
    public static final int STATE_SKIPPING_TO_PREVIOUS = 9;
    public static final int STATE_STOPPED = 1;
    private final long mActions;
    private final long mBufferedPosition;
    private final CharSequence mErrorMessage;
    private final long mPosition;
    private final float mSpeed;
    private final int mState;
    private Object mStateObj;
    private final long mUpdateTime;

    public static final class Builder {
        private long mActions;
        private long mBufferedPosition;
        private CharSequence mErrorMessage;
        private long mPosition;
        private float mRate;
        private int mState;
        private long mUpdateTime;

        public Builder(PlaybackStateCompat source) {
            this.mState = source.mState;
            this.mPosition = source.mPosition;
            this.mRate = source.mSpeed;
            this.mUpdateTime = source.mUpdateTime;
            this.mBufferedPosition = source.mBufferedPosition;
            this.mActions = source.mActions;
            this.mErrorMessage = source.mErrorMessage;
        }

        public void setState(int state, long position, float playbackRate) {
            this.mState = state;
            this.mPosition = position;
            this.mRate = playbackRate;
            this.mUpdateTime = SystemClock.elapsedRealtime();
        }

        public void setBufferedPosition(long bufferPosition) {
            this.mBufferedPosition = bufferPosition;
        }

        public void setActions(long capabilities) {
            this.mActions = capabilities;
        }

        public void setErrorMessage(CharSequence errorMessage) {
            this.mErrorMessage = errorMessage;
        }

        public PlaybackStateCompat build() {
            return new PlaybackStateCompat(this.mState, this.mPosition, this.mBufferedPosition, this.mRate, this.mActions, this.mErrorMessage, this.mUpdateTime, null);
        }
    }

    /* synthetic */ PlaybackStateCompat(int state, long position, long bufferedPosition, float rate, long actions, CharSequence errorMessage, long updateTime, PlaybackStateCompat -this7) {
        this(state, position, bufferedPosition, rate, actions, errorMessage, updateTime);
    }

    /* synthetic */ PlaybackStateCompat(Parcel in, PlaybackStateCompat -this1) {
        this(in);
    }

    private PlaybackStateCompat(int state, long position, long bufferedPosition, float rate, long actions, CharSequence errorMessage, long updateTime) {
        this.mState = state;
        this.mPosition = position;
        this.mBufferedPosition = bufferedPosition;
        this.mSpeed = rate;
        this.mActions = actions;
        this.mErrorMessage = errorMessage;
        this.mUpdateTime = updateTime;
    }

    private PlaybackStateCompat(Parcel in) {
        this.mState = in.readInt();
        this.mPosition = in.readLong();
        this.mSpeed = in.readFloat();
        this.mUpdateTime = in.readLong();
        this.mBufferedPosition = in.readLong();
        this.mActions = in.readLong();
        this.mErrorMessage = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
    }

    public String toString() {
        StringBuilder bob = new StringBuilder("PlaybackState {");
        bob.append("state=").append(this.mState);
        bob.append(", position=").append(this.mPosition);
        bob.append(", buffered position=").append(this.mBufferedPosition);
        bob.append(", speed=").append(this.mSpeed);
        bob.append(", updated=").append(this.mUpdateTime);
        bob.append(", actions=").append(this.mActions);
        bob.append(", error=").append(this.mErrorMessage);
        bob.append("}");
        return bob.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mState);
        dest.writeLong(this.mPosition);
        dest.writeFloat(this.mSpeed);
        dest.writeLong(this.mUpdateTime);
        dest.writeLong(this.mBufferedPosition);
        dest.writeLong(this.mActions);
        TextUtils.writeToParcel(this.mErrorMessage, dest, flags);
    }

    public int getState() {
        return this.mState;
    }

    public long getPosition() {
        return this.mPosition;
    }

    public long getBufferedPosition() {
        return this.mBufferedPosition;
    }

    public float getPlaybackSpeed() {
        return this.mSpeed;
    }

    public long getActions() {
        return this.mActions;
    }

    public CharSequence getErrorMessage() {
        return this.mErrorMessage;
    }

    public long getLastPositionUpdateTime() {
        return this.mUpdateTime;
    }

    public static PlaybackStateCompat fromPlaybackState(Object stateObj) {
        if (stateObj == null || VERSION.SDK_INT < 21) {
            return null;
        }
        PlaybackStateCompat state = new PlaybackStateCompat(PlaybackStateCompatApi21.getState(stateObj), PlaybackStateCompatApi21.getPosition(stateObj), PlaybackStateCompatApi21.getBufferedPosition(stateObj), PlaybackStateCompatApi21.getPlaybackSpeed(stateObj), PlaybackStateCompatApi21.getActions(stateObj), PlaybackStateCompatApi21.getErrorMessage(stateObj), PlaybackStateCompatApi21.getLastPositionUpdateTime(stateObj));
        state.mStateObj = stateObj;
        return state;
    }

    public Object getPlaybackState() {
        if (this.mStateObj != null || VERSION.SDK_INT < 21) {
            return this.mStateObj;
        }
        this.mStateObj = PlaybackStateCompatApi21.newInstance(this.mState, this.mPosition, this.mBufferedPosition, this.mSpeed, this.mActions, this.mErrorMessage, this.mUpdateTime);
        return this.mStateObj;
    }
}
