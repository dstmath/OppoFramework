package android.media;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import com.oppo.media.OppoMediaMetadataRetriever;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

public class MediaMetadataRetriever implements AutoCloseable {
    private static final int EMBEDDED_PICTURE_TYPE_ANY = 65535;
    public static final int METADATA_KEY_ALBUM = 1;
    public static final int METADATA_KEY_ALBUMARTIST = 13;
    public static final int METADATA_KEY_ARTIST = 2;
    public static final int METADATA_KEY_AUTHOR = 3;
    public static final int METADATA_KEY_BITRATE = 20;
    public static final int METADATA_KEY_BITS_PER_SAMPLE = 39;
    public static final int METADATA_KEY_CAPTURE_FRAMERATE = 25;
    public static final int METADATA_KEY_CD_TRACK_NUMBER = 0;
    public static final int METADATA_KEY_COLOR_RANGE = 37;
    public static final int METADATA_KEY_COLOR_STANDARD = 35;
    public static final int METADATA_KEY_COLOR_TRANSFER = 36;
    public static final int METADATA_KEY_COMPILATION = 15;
    public static final int METADATA_KEY_COMPOSER = 4;
    public static final int METADATA_KEY_DATE = 5;
    public static final int METADATA_KEY_DISC_NUMBER = 14;
    public static final int METADATA_KEY_DURATION = 9;
    public static final int METADATA_KEY_EXIF_LENGTH = 34;
    public static final int METADATA_KEY_EXIF_OFFSET = 33;
    public static final int METADATA_KEY_GENRE = 6;
    public static final int METADATA_KEY_HAS_AUDIO = 16;
    public static final int METADATA_KEY_HAS_IMAGE = 26;
    public static final int METADATA_KEY_HAS_VIDEO = 17;
    public static final int METADATA_KEY_IMAGE_COUNT = 27;
    public static final int METADATA_KEY_IMAGE_HEIGHT = 30;
    public static final int METADATA_KEY_IMAGE_PRIMARY = 28;
    public static final int METADATA_KEY_IMAGE_ROTATION = 31;
    public static final int METADATA_KEY_IMAGE_WIDTH = 29;
    public static final int METADATA_KEY_IS_DRM = 22;
    public static final int METADATA_KEY_LOCATION = 23;
    public static final int METADATA_KEY_MIMETYPE = 12;
    public static final int METADATA_KEY_NUM_TRACKS = 10;
    public static final int METADATA_KEY_SAMPLERATE = 38;
    public static final int METADATA_KEY_TIMED_TEXT_LANGUAGES = 21;
    public static final int METADATA_KEY_TITLE = 7;
    public static final int METADATA_KEY_VIDEO_FRAME_COUNT = 32;
    public static final int METADATA_KEY_VIDEO_HEIGHT = 19;
    public static final int METADATA_KEY_VIDEO_ROTATION = 24;
    public static final int METADATA_KEY_VIDEO_WIDTH = 18;
    public static final int METADATA_KEY_WRITER = 11;
    public static final int METADATA_KEY_YEAR = 8;
    public static final int OPTION_CLOSEST = 3;
    public static final int OPTION_CLOSEST_SYNC = 2;
    public static final int OPTION_NEXT_SYNC = 1;
    public static final int OPTION_PREVIOUS_SYNC = 0;
    private long mNativeContext;
    private OppoMediaMetadataRetriever mOppoRetirver = new OppoMediaMetadataRetriever();

    @Retention(RetentionPolicy.SOURCE)
    public @interface Option {
    }

    private native List<Bitmap> _getFrameAtIndex(int i, int i2, BitmapParams bitmapParams);

    private native Bitmap _getFrameAtTime(long j, int i, int i2, int i3);

    private native Bitmap _getImageAtIndex(int i, BitmapParams bitmapParams);

    private native void _setDataSource(MediaDataSource mediaDataSource) throws IllegalArgumentException;

    private native void _setDataSource(IBinder iBinder, String str, String[] strArr, String[] strArr2) throws IllegalArgumentException;

    @UnsupportedAppUsage
    private native byte[] getEmbeddedPicture(int i);

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final native void native_finalize();

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private static native void native_init();

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private native void native_setup();

    public native void _release();

    public native void _setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IllegalArgumentException;

    public native String extractMetadata(int i);

    public native Bitmap getThumbnailImageAtIndex(int i, BitmapParams bitmapParams, int i2, int i3);

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    public MediaMetadataRetriever() {
        native_setup();
    }

    public void setDataSource(String path) throws IllegalArgumentException {
        this.mOppoRetirver.mPath = path;
        if (path != null) {
            FileInputStream is = null;
            try {
                is = new FileInputStream(path);
                setDataSource(is.getFD(), 0, 576460752303423487L);
                try {
                    is.close();
                } catch (Exception e) {
                }
            } catch (FileNotFoundException e2) {
                throw new IllegalArgumentException();
            } catch (IOException e3) {
                throw new IllegalArgumentException();
            } catch (Throwable th) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e4) {
                    }
                }
                throw th;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setDataSource(String uri, Map<String, String> headers) throws IllegalArgumentException {
        int i = 0;
        String[] keys = new String[headers.size()];
        String[] values = new String[headers.size()];
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
        }
        _setDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(uri), uri, keys, values);
    }

    public void setDataSource(FileDescriptor fd, long offset, long length) throws IllegalArgumentException {
        this.mOppoRetirver.preSetDataSource(fd, offset, length);
        _setDataSource(fd, offset, length);
    }

    public void setDataSource(FileDescriptor fd) throws IllegalArgumentException {
        setDataSource(fd, 0, 576460752303423487L);
    }

    public void setDataSource(Context context, Uri uri) throws IllegalArgumentException, SecurityException {
        if (uri != null) {
            String scheme = uri.getScheme();
            if (scheme == null || scheme.equals(ContentResolver.SCHEME_FILE)) {
                setDataSource(uri.getPath());
                return;
            }
            AssetFileDescriptor fd = null;
            try {
                try {
                    AssetFileDescriptor fd2 = context.getContentResolver().openAssetFileDescriptor(uri, "r");
                    if (fd2 != null) {
                        FileDescriptor descriptor = fd2.getFileDescriptor();
                        if (descriptor.valid()) {
                            if (fd2.getDeclaredLength() < 0) {
                                setDataSource(descriptor);
                            } else {
                                setDataSource(descriptor, fd2.getStartOffset(), fd2.getDeclaredLength());
                            }
                            try {
                                fd2.close();
                            } catch (IOException e) {
                            }
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else {
                        throw new IllegalArgumentException();
                    }
                } catch (FileNotFoundException e2) {
                    throw new IllegalArgumentException();
                }
            } catch (SecurityException e3) {
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e4) {
                    }
                }
                setDataSource(uri.toString());
            } catch (Throwable th) {
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException {
        _setDataSource(dataSource);
    }

    public Bitmap getFrameAtTime(long timeUs, int option) {
        if (option < 0 || option > 3) {
            throw new IllegalArgumentException("Unsupported option: " + option);
        }
        Bitmap bitmap = _getFrameAtTime(timeUs, option, -1, -1);
        if (bitmap == null) {
            return this.mOppoRetirver.getFrameAtTimeWithOppoIfNeeded(timeUs, option);
        }
        return bitmap;
    }

    public Bitmap getScaledFrameAtTime(long timeUs, int option, int dstWidth, int dstHeight) {
        if (option < 0 || option > 3) {
            throw new IllegalArgumentException("Unsupported option: " + option);
        } else if (dstWidth <= 0) {
            throw new IllegalArgumentException("Invalid width: " + dstWidth);
        } else if (dstHeight > 0) {
            Bitmap bitmap = _getFrameAtTime(timeUs, option, dstWidth, dstHeight);
            if (bitmap == null) {
                return this.mOppoRetirver.getFrameAtTimeWithOppoIfNeeded(timeUs, option);
            }
            return bitmap;
        } else {
            throw new IllegalArgumentException("Invalid height: " + dstHeight);
        }
    }

    public Bitmap getFrameAtTime(long timeUs) {
        return getFrameAtTime(timeUs, 2);
    }

    public Bitmap getFrameAtTime() {
        return getFrameAtTime(-1, 2);
    }

    public static final class BitmapParams {
        private Bitmap.Config inPreferredConfig = Bitmap.Config.ARGB_8888;
        private Bitmap.Config outActualConfig = Bitmap.Config.ARGB_8888;

        public void setPreferredConfig(Bitmap.Config config) {
            if (config != null) {
                this.inPreferredConfig = config;
                return;
            }
            throw new IllegalArgumentException("preferred config can't be null");
        }

        public Bitmap.Config getPreferredConfig() {
            return this.inPreferredConfig;
        }

        public Bitmap.Config getActualConfig() {
            return this.outActualConfig;
        }
    }

    public Bitmap getFrameAtIndex(int frameIndex, BitmapParams params) {
        return getFramesAtIndex(frameIndex, 1, params).get(0);
    }

    public Bitmap getFrameAtIndex(int frameIndex) {
        return getFramesAtIndex(frameIndex, 1).get(0);
    }

    public List<Bitmap> getFramesAtIndex(int frameIndex, int numFrames, BitmapParams params) {
        return getFramesAtIndexInternal(frameIndex, numFrames, params);
    }

    public List<Bitmap> getFramesAtIndex(int frameIndex, int numFrames) {
        return getFramesAtIndexInternal(frameIndex, numFrames, null);
    }

    private List<Bitmap> getFramesAtIndexInternal(int frameIndex, int numFrames, BitmapParams params) {
        if ("yes".equals(extractMetadata(17))) {
            int frameCount = Integer.parseInt(extractMetadata(32));
            if (frameIndex >= 0 && numFrames >= 1 && frameIndex < frameCount && frameIndex <= frameCount - numFrames) {
                return _getFrameAtIndex(frameIndex, numFrames, params);
            }
            throw new IllegalArgumentException("Invalid frameIndex or numFrames: " + frameIndex + ", " + numFrames);
        }
        throw new IllegalStateException("Does not contail video or image sequences");
    }

    public Bitmap getImageAtIndex(int imageIndex, BitmapParams params) {
        return getImageAtIndexInternal(imageIndex, params);
    }

    public Bitmap getImageAtIndex(int imageIndex) {
        return getImageAtIndexInternal(imageIndex, null);
    }

    public Bitmap getPrimaryImage(BitmapParams params) {
        return getImageAtIndexInternal(-1, params);
    }

    public Bitmap getPrimaryImage() {
        return getImageAtIndexInternal(-1, null);
    }

    private Bitmap getImageAtIndexInternal(int imageIndex, BitmapParams params) {
        if ("yes".equals(extractMetadata(26))) {
            String imageCount = extractMetadata(27);
            if (imageIndex < Integer.parseInt(imageCount)) {
                return _getImageAtIndex(imageIndex, params);
            }
            throw new IllegalArgumentException("Invalid image index: " + imageCount);
        }
        throw new IllegalStateException("Does not contail still images");
    }

    public byte[] getEmbeddedPicture() {
        return getEmbeddedPicture(65535);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        release();
    }

    public void release() {
        OppoMediaMetadataRetriever oppoMediaMetadataRetriever = this.mOppoRetirver;
        if (oppoMediaMetadataRetriever != null) {
            oppoMediaMetadataRetriever.closeFd();
            OppoMediaMetadataRetriever oppoMediaMetadataRetriever2 = this.mOppoRetirver;
            oppoMediaMetadataRetriever2.mPath = null;
            oppoMediaMetadataRetriever2.release();
            this.mOppoRetirver = null;
        }
        _release();
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }
}
