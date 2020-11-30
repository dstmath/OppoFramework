package android.support.v4.media.session;

import android.media.session.PlaybackState;
import android.os.Bundle;
import java.util.List;

/* access modifiers changed from: package-private */
public class PlaybackStateCompatApi21 {
    public static int getState(Object stateObj) {
        return ((PlaybackState) stateObj).getState();
    }

    public static long getPosition(Object stateObj) {
        return ((PlaybackState) stateObj).getPosition();
    }

    public static long getBufferedPosition(Object stateObj) {
        return ((PlaybackState) stateObj).getBufferedPosition();
    }

    public static float getPlaybackSpeed(Object stateObj) {
        return ((PlaybackState) stateObj).getPlaybackSpeed();
    }

    public static long getActions(Object stateObj) {
        return ((PlaybackState) stateObj).getActions();
    }

    public static CharSequence getErrorMessage(Object stateObj) {
        return ((PlaybackState) stateObj).getErrorMessage();
    }

    public static long getLastPositionUpdateTime(Object stateObj) {
        return ((PlaybackState) stateObj).getLastPositionUpdateTime();
    }

    public static List<Object> getCustomActions(Object stateObj) {
        return ((PlaybackState) stateObj).getCustomActions();
    }

    public static long getActiveQueueItemId(Object stateObj) {
        return ((PlaybackState) stateObj).getActiveQueueItemId();
    }

    /* access modifiers changed from: package-private */
    public static final class CustomAction {
        public static String getAction(Object customActionObj) {
            return ((PlaybackState.CustomAction) customActionObj).getAction();
        }

        public static CharSequence getName(Object customActionObj) {
            return ((PlaybackState.CustomAction) customActionObj).getName();
        }

        public static int getIcon(Object customActionObj) {
            return ((PlaybackState.CustomAction) customActionObj).getIcon();
        }

        public static Bundle getExtras(Object customActionObj) {
            return ((PlaybackState.CustomAction) customActionObj).getExtras();
        }
    }
}
