package com.mediatek.dcfdecoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.MemoryFile;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DcfDecoder {
    private static final int ACTION_DECODE_FULL_IMAGE = 0;
    private static final int ACTION_JUST_DECODE_BOUND = 1;
    private static final int ACTION_JUST_DECODE_THUMBNAIL = 2;
    private static final int DECODE_THUMBNAIL_FLAG = 256;
    private static final int HEADER_BUFFER_SIZE = 128;
    private static final String TAG = "DRM/DcfDecoder";
    private static final int THUMBNAIL_TARGET_SIZE = 96;
    private static boolean sIsOmaDrmEnabled;

    private static native byte[] nativeDecryptDcfFile(FileDescriptor fileDescriptor, int i, int i2);

    private native byte[] nativeForceDecryptFile(String str, boolean z);

    static {
        sIsOmaDrmEnabled = false;
        sIsOmaDrmEnabled = true;
        if (sIsOmaDrmEnabled) {
            System.loadLibrary("dcfdecoderjni");
        }
    }

    public byte[] forceDecryptFile(String pathName, boolean consume) {
        if (pathName != null) {
            return nativeForceDecryptFile(pathName, consume);
        }
        Log.e(TAG, "forceDecryptFile: find null file name!");
        return null;
    }

    public static Bitmap decodeDrmImageIfNeeded(byte[] header, InputStream left, BitmapFactory.Options opts) {
        try {
            Log.d(TAG, "decodeDrmImageIfNeeded with stream left [" + left.available() + "]");
            return decodeDrmImageIfNeeded(((FileInputStream) left).getFD(), opts);
        } catch (IOException e) {
            Log.e(TAG, "decodeDrmImageIfNeeded stream caught IOException ");
            return null;
        } catch (ClassCastException e2) {
            Log.e(TAG, "decodeDrmImageIfNeeded stream caught ClassCastException ");
            return null;
        }
    }

    public static Bitmap decodeDrmImageIfNeeded(FileDescriptor fd, BitmapFactory.Options opts) {
        if (!sIsOmaDrmEnabled) {
            return null;
        }
        if (opts != null && opts.inJustDecodeBounds && opts.outWidth > 0 && opts.outHeight > 0) {
            return null;
        }
        Log.d(TAG, "decodeDrmImageIfNeeded with fd");
        long offset = -1;
        try {
            offset = Os.lseek(fd, 0, OsConstants.SEEK_CUR);
            Bitmap decodeDrmImage = decodeDrmImage(fd, 0, opts);
            if (offset != -1) {
                try {
                    Os.lseek(fd, offset, OsConstants.SEEK_SET);
                } catch (ErrnoException errno1) {
                    Log.e(TAG, "decodeDrmImageIfNeeded seek fd to initial offset with ", errno1);
                }
            }
            return decodeDrmImage;
        } catch (ErrnoException errno) {
            Log.e(TAG, "decodeDrmImageIfNeeded seek fd to beginning with ", errno);
            if (offset != -1) {
                try {
                    Os.lseek(fd, offset, OsConstants.SEEK_SET);
                } catch (ErrnoException errno12) {
                    Log.e(TAG, "decodeDrmImageIfNeeded seek fd to initial offset with ", errno12);
                }
            }
            return null;
        } catch (Throwable th) {
            if (offset != -1) {
                try {
                    Os.lseek(fd, offset, OsConstants.SEEK_SET);
                } catch (ErrnoException errno13) {
                    Log.e(TAG, "decodeDrmImageIfNeeded seek fd to initial offset with ", errno13);
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004e, code lost:
        if (r2 == null) goto L_0x0051;
     */
    public static Bitmap decodeDrmImageIfNeeded(byte[] data, BitmapFactory.Options opts) {
        if (!sIsOmaDrmEnabled) {
            return null;
        }
        if (opts != null && opts.inJustDecodeBounds && opts.outWidth > 0 && opts.outHeight > 0) {
            return null;
        }
        Log.d(TAG, "decodeDrmImageIfNeeded with data");
        if (!isDrmFile(data)) {
            return null;
        }
        Bitmap bm = null;
        MemoryFile ashemem = null;
        try {
            ashemem = new MemoryFile("drm_image", data.length);
            ashemem.writeBytes(data, 0, 0, data.length);
            bm = decodeDrmImage(ashemem.getFileDescriptor(), ashemem.length(), opts);
        } catch (IOException e) {
            Log.e(TAG, "decodeDrmImageIfNeeded with ", e);
        } catch (Throwable th) {
            if (ashemem != null) {
                ashemem.close();
            }
            throw th;
        }
        ashemem.close();
        return bm;
    }

    private static Bitmap decodeDrmImage(FileDescriptor fd, int size, BitmapFactory.Options opts) {
        int action = 0;
        if (opts != null) {
            if (opts.inJustDecodeBounds) {
                action = 1;
            } else if ((opts.inSampleSize & 256) > 0) {
                action = 2;
            }
        }
        byte[] clearData = nativeDecryptDcfFile(fd, size, action);
        if (clearData == null) {
            Log.e(TAG, "decodeDrmImage native decrypt failed ");
            return null;
        }
        if (action == 2) {
            BitmapFactory.Options thumbnailOpts = new BitmapFactory.Options();
            thumbnailOpts.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(clearData, 0, clearData.length, thumbnailOpts);
            opts.inSampleSize = Math.min(thumbnailOpts.outWidth / THUMBNAIL_TARGET_SIZE, thumbnailOpts.outHeight / THUMBNAIL_TARGET_SIZE);
        }
        return BitmapFactory.decodeByteArray(clearData, 0, clearData.length, opts);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.lang.String.<init>(byte[], int, int):void}
     arg types: [byte[], int, byte]
     candidates:
      ClspMth{java.lang.String.<init>(int[], int, int):void}
      ClspMth{java.lang.String.<init>(char[], int, int):void}
      ClspMth{java.lang.String.<init>(byte[], int, int):void} */
    private static boolean isDrmFile(byte[] header) {
        if (header == null || header.length < HEADER_BUFFER_SIZE) {
            return false;
        }
        String magic = new String(header, 0, 8);
        if (magic.startsWith("CTA5")) {
            Log.d(TAG, "isDrmFile: this is a cta5 file: " + magic);
            return true;
        } else if (header[0] != 1) {
            Log.d(TAG, "isDrmFile: version is not dcf version 1, no oma drm file");
            return false;
        } else {
            byte b = header[1];
            byte b2 = header[2];
            if (b <= 0 || b + 3 > HEADER_BUFFER_SIZE || b2 <= 0 || b2 > HEADER_BUFFER_SIZE) {
                Log.d(TAG, "isDrmFile: content type or uri len invalid, not oma drm file, contentType[" + ((int) b) + "] contentUri[" + ((int) b2) + "]");
                return false;
            }
            String contentType = new String(header, 3, (int) b);
            if (!contentType.contains("/")) {
                Log.d(TAG, "isDrmFile: content type not right, not oma drm file");
                return false;
            }
            Log.d(TAG, "this is a oma drm file: " + contentType);
            return true;
        }
    }
}
