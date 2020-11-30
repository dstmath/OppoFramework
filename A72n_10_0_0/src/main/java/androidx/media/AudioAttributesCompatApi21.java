package androidx.media;

import android.media.AudioAttributes;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class AudioAttributesCompatApi21 {
    private static Method sAudioAttributesToLegacyStreamType;

    public static int toLegacyStreamType(Wrapper aaWrap) {
        AudioAttributes aaObject = aaWrap.unwrap();
        try {
            if (sAudioAttributesToLegacyStreamType == null) {
                sAudioAttributesToLegacyStreamType = AudioAttributes.class.getMethod("toLegacyStreamType", AudioAttributes.class);
            }
            return ((Integer) sAudioAttributesToLegacyStreamType.invoke(null, aaObject)).intValue();
        } catch (ClassCastException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.w("AudioAttributesCompat", "getLegacyStreamType() failed on API21+", e);
            return -1;
        }
    }

    static final class Wrapper {
        private AudioAttributes mWrapped;

        private Wrapper(AudioAttributes obj) {
            this.mWrapped = obj;
        }

        public static Wrapper wrap(AudioAttributes obj) {
            if (obj != null) {
                return new Wrapper(obj);
            }
            throw new IllegalArgumentException("AudioAttributesApi21.Wrapper cannot wrap null");
        }

        public AudioAttributes unwrap() {
            return this.mWrapped;
        }
    }
}
