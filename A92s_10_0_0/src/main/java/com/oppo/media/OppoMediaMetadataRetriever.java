package com.oppo.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.system.Os;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class OppoMediaMetadataRetriever {
    private static final int EMBEDDED_PICTURE_TYPE_ANY = 65535;
    public static final int METADATA_KEY_ALBUM = 1;
    public static final int METADATA_KEY_ALBUMARTIST = 13;
    public static final int METADATA_KEY_ARTIST = 2;
    public static final int METADATA_KEY_AUTHOR = 3;
    public static final int METADATA_KEY_BITRATE = 20;
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
    public static final int METADATA_KEY_VIDEO_WIDTH = 18;
    public static final int METADATA_KEY_WRITER = 11;
    public static final int METADATA_KEY_YEAR = 8;
    public static final int OPTION_CLOSEST = 3;
    public static final int OPTION_CLOSEST_SYNC = 2;
    public static final int OPTION_NEXT_SYNC = 1;
    public static final int OPTION_PREVIOUS_SYNC = 0;
    public static final String TAG = "OppoMediaMetadataRetriever";
    public FileDescriptor mFd = null;
    public long mLength = 0;
    private long mNativeContext;
    public long mOffset = 0;
    public String mPath = null;

    private native Bitmap _getFrameAtTime(long j, int i);

    private native void _setDataSource(String str, String[] strArr, String[] strArr2) throws IllegalArgumentException;

    private native byte[] getEmbeddedPicture(int i);

    private final native void native_finalize();

    private static native void native_init();

    private native void native_setup();

    public native String extractMetadata(int i);

    public native void release();

    public native void setDataSource(FileDescriptor fileDescriptor, long j, long j2) throws IllegalArgumentException;

    public native void setLocale(String str) throws IllegalArgumentException;

    static {
        System.loadLibrary("oppostagefright");
        System.loadLibrary("oppometadataretriever_jni");
        native_init();
    }

    public OppoMediaMetadataRetriever() {
        native_setup();
    }

    public void setDataSource(String path) throws IllegalArgumentException {
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
    }

    public void closeFd() {
        try {
            Log.v(TAG, "closeFd()");
            if (this.mFd != null && this.mFd.valid()) {
                Log.v(TAG, "mFd is valid, close it.");
                Os.close(this.mFd);
                this.mFd = null;
                this.mOffset = -1;
                this.mLength = -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception = " + e);
            e.printStackTrace();
        }
    }

    public void preSetDataSource(FileDescriptor fd, long offset, long length) {
        this.mOffset = offset;
        this.mLength = length;
        try {
            closeFd();
            this.mFd = Os.dup(fd);
            this.mOffset = offset;
            this.mLength = length;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            ex.printStackTrace();
        }
    }

    public Bitmap getFrameAtTimeWithOppoIfNeeded(long timeUs, int option) {
        Log.d(TAG, "getFrameAtTime return NULL, try oppomedia");
        try {
            if (this.mPath != null) {
                setDataSource(this.mPath);
            } else if (this.mFd != null && this.mFd.valid()) {
                setDataSource(this.mFd, this.mOffset, this.mLength);
            }
            Bitmap bitmap = getFrameAtTime(timeUs, option);
            closeFd();
            return bitmap;
        } catch (Exception ex) {
            Log.w(TAG, "mOppoMetaDataRetriver setDataSource error");
            closeFd();
            ex.printStackTrace();
            return null;
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
        _setDataSource(uri, keys, values);
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

    public Bitmap getFrameAtTime(long timeUs, int option) {
        if (option >= 0 && option <= 3) {
            return _getFrameAtTime(timeUs, option);
        }
        throw new IllegalArgumentException("Unsupported option: " + option);
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

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }
}
