package android.media;

import android.app.backup.FullBackup;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import com.oppo.media.OppoMediaMetadataRetriever;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import libcore.io.Libcore;

public class MediaMetadataRetriever {
    private static final int EMBEDDED_PICTURE_TYPE_ANY = 65535;
    public static final int METADATA_KEY_ALBUM = 1;
    public static final int METADATA_KEY_ALBUMARTIST = 13;
    public static final int METADATA_KEY_ARTIST = 2;
    public static final int METADATA_KEY_AUTHOR = 3;
    public static final int METADATA_KEY_BITRATE = 20;
    public static final int METADATA_KEY_CAPTURE_FRAMERATE = 25;
    public static final int METADATA_KEY_CD_TRACK_NUMBER = 0;
    public static final int METADATA_KEY_COMPILATION = 15;
    public static final int METADATA_KEY_COMPOSER = 4;
    public static final int METADATA_KEY_DATE = 5;
    public static final int METADATA_KEY_DISC_NUMBER = 14;
    public static final int METADATA_KEY_DURATION = 9;
    public static final int METADATA_KEY_GENRE = 6;
    public static final int METADATA_KEY_HAS_AUDIO = 16;
    public static final int METADATA_KEY_HAS_VIDEO = 17;
    public static final int METADATA_KEY_IS_DRM = 22;
    public static final int METADATA_KEY_LOCATION = 23;
    public static final int METADATA_KEY_MIMETYPE = 12;
    public static final int METADATA_KEY_NUM_TRACKS = 10;
    public static final int METADATA_KEY_TIMED_TEXT_LANGUAGES = 21;
    public static final int METADATA_KEY_TITLE = 7;
    public static final int METADATA_KEY_VIDEO_HEIGHT = 19;
    public static final int METADATA_KEY_VIDEO_ROTATION = 24;
    public static final int METADATA_KEY_VIDEO_WIDTH = 18;
    public static final int METADATA_KEY_WRITER = 11;
    public static final int METADATA_KEY_YEAR = 8;
    public static final int OPTION_CLOSEST = 3;
    public static final int OPTION_CLOSEST_SYNC = 2;
    public static final int OPTION_NEXT_SYNC = 1;
    public static final int OPTION_PREVIOUS_SYNC = 0;
    private static final String TAG = "MediaMetadataRetriever";
    private FileDescriptor mFd = null;
    private long mLength = 0;
    private long mNativeContext;
    private long mOffset = 0;
    private OppoMediaMetadataRetriever mOppoRetirver = null;
    private String mPath = null;

    private native Bitmap _getFrameAtTime(long j, int i, int i2, int i3);

    private native void _setDataSource(MediaDataSource mediaDataSource) throws IllegalArgumentException;

    private native void _setDataSource(MediaDataSource mediaDataSource, String str) throws IllegalArgumentException;

    private native void _setDataSource(IBinder iBinder, String str, String[] strArr, String[] strArr2) throws IllegalArgumentException;

    private native byte[] getEmbeddedPicture(int i);

    private final native void native_finalize();

    private static native void native_init();

    private native void native_setup();

    public native void _release();

    public native void _setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IllegalArgumentException;

    public native String extractMetadata(int i);

    static {
        System.loadLibrary("media_jni");
        native_init();
    }

    public MediaMetadataRetriever() {
        native_setup();
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0031 A:{SYNTHETIC, Splitter: B:19:0x0031} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDataSource(String path) throws IllegalArgumentException {
        Throwable th;
        this.mPath = path;
        if (path == null) {
            throw new IllegalArgumentException();
        }
        FileInputStream is = null;
        try {
            FileInputStream is2 = new FileInputStream(path);
            try {
                setDataSource(is2.getFD(), 0, 576460752303423487L);
                if (is2 != null) {
                    try {
                        is2.close();
                    } catch (Exception e) {
                    }
                }
            } catch (FileNotFoundException e2) {
                is = is2;
                throw new IllegalArgumentException();
            } catch (IOException e3) {
                is = is2;
                try {
                    throw new IllegalArgumentException();
                } catch (Throwable th2) {
                    th = th2;
                    if (is != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                is = is2;
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e4) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            throw new IllegalArgumentException();
        } catch (IOException e6) {
            throw new IllegalArgumentException();
        }
    }

    public void setDataSource(String uri, Map<String, String> headers) throws IllegalArgumentException {
        int i = 0;
        String[] keys = new String[headers.size()];
        String[] values = new String[headers.size()];
        for (Entry<String, String> entry : headers.entrySet()) {
            keys[i] = (String) entry.getKey();
            values[i] = (String) entry.getValue();
            i++;
        }
        _setDataSource(MediaHTTPService.createHttpServiceBinderIfNecessary(uri), uri, keys, values);
    }

    public void setDataSource(FileDescriptor fd, long offset, long length) throws IllegalArgumentException {
        this.mOffset = offset;
        this.mLength = length;
        try {
            closeFd();
            this.mFd = Libcore.os.dup(fd);
            this.mOffset = offset;
            this.mLength = length;
        } catch (Exception ex) {
            closeFd();
            Log.e(TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        }
        _setDataSource(fd, offset, length);
    }

    public void setDataSource(FileDescriptor fd) throws IllegalArgumentException {
        setDataSource(fd, 0, 576460752303423487L);
    }

    public void setDataSource(Context context, Uri uri) throws IllegalArgumentException, SecurityException {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals(ContentResolver.SCHEME_FILE)) {
            setDataSource(uri.getPath());
            return;
        }
        AssetFileDescriptor fd = null;
        try {
            fd = context.getContentResolver().openAssetFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
            if (fd == null) {
                throw new IllegalArgumentException();
            }
            FileDescriptor descriptor = fd.getFileDescriptor();
            if (descriptor.valid()) {
                if (fd.getDeclaredLength() < 0) {
                    setDataSource(descriptor);
                } else {
                    setDataSource(descriptor, fd.getStartOffset(), fd.getDeclaredLength());
                }
                if (fd != null) {
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                }
                return;
            }
            throw new IllegalArgumentException();
        } catch (FileNotFoundException e2) {
            throw new IllegalArgumentException();
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
        }
    }

    public void setDataSource(MediaDataSource dataSource) throws IllegalArgumentException {
        _setDataSource(dataSource);
    }

    public void setDataSource(MediaDataSource dataSource, String mime) throws IllegalArgumentException {
        _setDataSource(dataSource, mime);
    }

    public Bitmap getFrameAtTime(long timeUs, int option) {
        if (option < 0 || option > 3) {
            throw new IllegalArgumentException("Unsupported option: " + option);
        }
        Bitmap bitmap = _getFrameAtTime(timeUs, option, -1, -1);
        if (bitmap == null) {
            Log.d(TAG, "getFrameAtTime return NULL, try oppomedia");
            if (this.mOppoRetirver == null) {
                try {
                    this.mOppoRetirver = new OppoMediaMetadataRetriever();
                } catch (UnsatisfiedLinkError e) {
                    Log.w(TAG, "UnsatisfiedLinkError");
                    closeFd();
                    return null;
                }
            }
            try {
                if (this.mPath != null) {
                    this.mOppoRetirver.setDataSource(this.mPath);
                } else if (this.mFd != null && this.mFd.valid()) {
                    this.mOppoRetirver.setDataSource(this.mFd, this.mOffset, this.mLength);
                }
                bitmap = this.mOppoRetirver.getFrameAtTime(timeUs, option);
            } catch (Exception ex) {
                Log.w(TAG, "mOppoMetaDataRetriver setDataSource error");
                closeFd();
                ex.printStackTrace();
                return null;
            }
        }
        closeFd();
        return bitmap;
    }

    public Bitmap getScaledFrameAtTime(long timeUs, int option, int dstWidth, int dstHeight) {
        if (option < 0 || option > 3) {
            throw new IllegalArgumentException("Unsupported option: " + option);
        } else if (dstWidth <= 0) {
            throw new IllegalArgumentException("Invalid width: " + dstWidth);
        } else if (dstHeight > 0) {
            return _getFrameAtTime(timeUs, option, dstWidth, dstHeight);
        } else {
            throw new IllegalArgumentException("Invalid height: " + dstHeight);
        }
    }

    private void closeFd() {
        try {
            Log.v(TAG, "closeFd()");
            if (this.mFd != null && this.mFd.valid()) {
                Log.v(TAG, "mFd is valid, close it.");
                Libcore.os.close(this.mFd);
                this.mFd = null;
                this.mOffset = -1;
                this.mLength = -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception = " + e);
            e.printStackTrace();
        }
    }

    public Bitmap getFrameAtTime(long timeUs) {
        return getFrameAtTime(timeUs, 2);
    }

    public Bitmap getFrameAtTime() {
        return getFrameAtTime(-1, 2);
    }

    public byte[] getEmbeddedPicture() {
        return getEmbeddedPicture(65535);
    }

    public void release() {
        if (this.mOppoRetirver != null) {
            this.mOppoRetirver.release();
            this.mOppoRetirver = null;
        }
        closeFd();
        this.mPath = null;
        _release();
    }

    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }
}
