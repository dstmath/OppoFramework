package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.common.OppoFeatureCache;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.util.TypedValue;
import com.color.darkmode.IColorDarkModeManager;
import com.mediatek.dcfDecoder.MTKDcfDecoderFactory;
import com.mediatek.dcfDecoder.MTKDcfDecoderManager;
import com.oppo.hypnus.Hypnus;
import com.oppo.hypnus.HypnusManager;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapFactory extends OppoBaseBitmapFactory {
    private static final int DECODE_BUFFER_SIZE = 16384;
    private static HypnusManager mHM = null;
    private static MTKDcfDecoderManager sMTKDcfDecoderManager = MTKDcfDecoderFactory.getInstance().makeMTKDcfDecoderManager();

    @UnsupportedAppUsage
    private static native Bitmap nativeDecodeAsset(long j, Rect rect, Options options, long j2, long j3);

    @UnsupportedAppUsage
    private static native Bitmap nativeDecodeByteArray(byte[] bArr, int i, int i2, Options options, long j, long j2);

    @UnsupportedAppUsage
    private static native Bitmap nativeDecodeFileDescriptor(FileDescriptor fileDescriptor, Rect rect, Options options, long j, long j2);

    @UnsupportedAppUsage
    private static native Bitmap nativeDecodeStream(InputStream inputStream, byte[] bArr, Rect rect, Options options, long j, long j2);

    private static native boolean nativeIsSeekable(FileDescriptor fileDescriptor);

    public static class Options {
        public Bitmap inBitmap;
        public int inDensity;
        public boolean inDither;
        @Deprecated
        public boolean inInputShareable;
        public boolean inJustDecodeBounds;
        public boolean inMutable;
        public boolean inPostProc = false;
        public int inPostProcFlag = 0;
        @Deprecated
        public boolean inPreferQualityOverSpeed;
        public ColorSpace inPreferredColorSpace = null;
        public Bitmap.Config inPreferredConfig = Bitmap.Config.ARGB_8888;
        public boolean inPremultiplied = true;
        @Deprecated
        public boolean inPurgeable;
        public int inSampleSize;
        public boolean inScaled = true;
        public int inScreenDensity;
        public int inTargetDensity;
        public byte[] inTempStorage;
        @Deprecated
        public boolean mCancel;
        public ColorSpace outColorSpace;
        public Bitmap.Config outConfig;
        public int outHeight;
        public String outMimeType;
        public int outWidth;

        @Deprecated
        public void requestCancelDecode() {
            this.mCancel = true;
        }

        static void validate(Options opts) {
            if (opts != null) {
                Bitmap bitmap = opts.inBitmap;
                if (bitmap != null) {
                    if (bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                        throw new IllegalArgumentException("Bitmaps with Config.HARDWARE are always immutable");
                    } else if (opts.inBitmap.isRecycled()) {
                        throw new IllegalArgumentException("Cannot reuse a recycled Bitmap");
                    }
                }
                if (!opts.inMutable || opts.inPreferredConfig != Bitmap.Config.HARDWARE) {
                    ColorSpace colorSpace = opts.inPreferredColorSpace;
                    if (colorSpace == null) {
                        return;
                    }
                    if (!(colorSpace instanceof ColorSpace.Rgb)) {
                        throw new IllegalArgumentException("The destination color space must use the RGB color model");
                    } else if (((ColorSpace.Rgb) colorSpace).getTransferParameters() == null) {
                        throw new IllegalArgumentException("The destination color space must use an ICC parametric transfer function");
                    }
                } else {
                    throw new IllegalArgumentException("Bitmaps with Config.HARDWARE cannot be decoded into - they are immutable");
                }
            }
        }

        static long nativeInBitmap(Options opts) {
            Bitmap bitmap;
            if (opts == null || (bitmap = opts.inBitmap) == null) {
                return 0;
            }
            return bitmap.getNativeInstance();
        }

        static long nativeColorSpace(Options opts) {
            ColorSpace colorSpace;
            if (opts == null || (colorSpace = opts.inPreferredColorSpace) == null) {
                return 0;
            }
            return colorSpace.getNativeInstance();
        }
    }

    public static Bitmap decodeFile(String pathName, Options opts) {
        Options.validate(opts);
        Bitmap bm = null;
        InputStream stream = null;
        try {
            stream = new FileInputStream(pathName);
            bm = decodeStream(stream, null, opts);
            try {
                stream.close();
            } catch (IOException e) {
            }
        } catch (Exception e2) {
            Log.e("BitmapFactory", "Unable to decode stream: " + e2);
            if (stream != null) {
                stream.close();
            }
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        return bm;
    }

    public static Bitmap decodeFile(String pathName) {
        return decodeFile(pathName, null);
    }

    public static Bitmap decodeResourceStream(Resources res, TypedValue value, InputStream is, Rect pad, Options opts) {
        Options.validate(opts);
        if (opts == null) {
            opts = new Options();
        }
        if (opts.inDensity == 0 && value != null) {
            int density = value.density;
            if (density == 0) {
                opts.inDensity = 160;
            } else if (density != 65535) {
                opts.inDensity = density;
            }
        }
        if (opts.inTargetDensity == 0 && res != null) {
            opts.inTargetDensity = res.getDisplayMetrics().densityDpi;
        }
        if (((IColorDarkModeManager) OppoFeatureCache.getOrCreate(IColorDarkModeManager.DEFAULT, new Object[0])).shouldIntercept()) {
            return ((IColorDarkModeManager) OppoFeatureCache.getOrCreate(IColorDarkModeManager.DEFAULT, new Object[0])).handleDecodeStream(is, pad, opts);
        }
        return decodeStream(is, pad, opts);
    }

    public static Bitmap decodeResource(Resources res, int id, Options opts) {
        String packageName;
        Bitmap bitmap;
        Options.validate(opts);
        Bitmap bm = null;
        InputStream is = null;
        try {
            TypedValue value = new TypedValue();
            is = res.openRawResource(id, value);
            bm = decodeResourceStream(res, value, is, null, opts);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        } catch (Exception e2) {
            if (is != null) {
                is.close();
            }
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        if (bm != null || opts == null || opts.inBitmap == null) {
            setAssetSourceAndHasCalculatedColor(bm, true, false);
            if (bm != null || (packageName = ActivityThread.currentPackageName()) == null || !packageName.equals("com.omichsoft.gallery") || (bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)) == null) {
                setAssetSourceAndHasCalculatedColor(bm, true, false);
                return bm;
            }
            bitmap.setNinePatchChunk(new byte[32]);
            return bitmap;
        }
        throw new IllegalArgumentException("Problem decoding into existing bitmap");
    }

    public static Bitmap decodeResource(Resources res, int id) {
        return decodeResource(res, id, null);
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length, Options opts) {
        if ((offset | length) < 0 || data.length < offset + length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        Options.validate(opts);
        Trace.traceBegin(2, "decodeBitmap");
        if (mHM == null && SystemClock.uptimeMillis() > 100000) {
            mHM = HypnusManager.getHypnusManager();
        }
        if (mHM != null && opts != null && !opts.inJustDecodeBounds && length > 524288) {
            mHM.hypnusSetSignatureAction(20, length >> 13, Hypnus.getLocalSignature());
        }
        try {
            Bitmap bm = nativeDecodeByteArray(data, offset, length, opts, Options.nativeInBitmap(opts), Options.nativeColorSpace(opts));
            if (bm == null && opts != null) {
                if (opts.inBitmap != null) {
                    throw new IllegalArgumentException("Problem decoding into existing bitmap");
                }
            }
            setDensityFromOptions(bm, opts);
            if (bm == null) {
                return sMTKDcfDecoderManager.decodeDrmImageIfNeededImpl(data, opts);
            }
            return bm;
        } finally {
            Trace.traceEnd(2);
        }
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
        return decodeByteArray(data, offset, length, null);
    }

    private static void setDensityFromOptions(Bitmap outputBitmap, Options opts) {
        if (outputBitmap != null && opts != null) {
            int density = opts.inDensity;
            if (density != 0) {
                outputBitmap.setDensity(density);
                int targetDensity = opts.inTargetDensity;
                if (targetDensity != 0 && density != targetDensity && density != opts.inScreenDensity) {
                    byte[] np = outputBitmap.getNinePatchChunk();
                    boolean isNinePatch = np != null && NinePatch.isNinePatchChunk(np);
                    if (opts.inScaled || isNinePatch) {
                        outputBitmap.setDensity(targetDensity);
                    }
                }
            } else if (opts.inBitmap != null) {
                outputBitmap.setDensity(Bitmap.getDefaultDensity());
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public static Bitmap decodeStream(InputStream is, Rect outPadding, Options opts) {
        Bitmap bm;
        if (is == null) {
            return null;
        }
        Options.validate(opts);
        Trace.traceBegin(2, "decodeBitmap");
        try {
            if (is instanceof AssetManager.AssetInputStream) {
                bm = nativeDecodeAsset(((AssetManager.AssetInputStream) is).getNativeAsset(), outPadding, opts, Options.nativeInBitmap(opts), Options.nativeColorSpace(opts));
                setIsAssetSource(bm, true);
            } else {
                bm = decodeStreamInternal(is, outPadding, opts);
            }
            if (bm == null && opts != null) {
                if (opts.inBitmap != null) {
                    throw new IllegalArgumentException("Problem decoding into existing bitmap");
                }
            }
            setDensityFromOptions(bm, opts);
            Trace.traceEnd(2);
            setHasCalculatedColor(bm, false);
            return bm;
        } catch (Throwable th) {
            Trace.traceEnd(2);
            throw th;
        }
    }

    private static Bitmap decodeStreamInternal(InputStream is, Rect outPadding, Options opts) {
        int size;
        byte[] tempStorage = null;
        if (opts != null) {
            tempStorage = opts.inTempStorage;
        }
        if (tempStorage == null) {
            tempStorage = new byte[16384];
        }
        if (mHM == null && SystemClock.uptimeMillis() > 100000) {
            mHM = HypnusManager.getHypnusManager();
        }
        try {
            size = is.available();
        } catch (IOException e) {
            Log.e("BitmapFactory", "decodeStreamInternal is.available err!");
            size = 0;
        }
        if (mHM != null && opts != null && !opts.inJustDecodeBounds && size > 524288) {
            mHM.hypnusSetSignatureAction(20, size >> 13, Hypnus.getLocalSignature());
        }
        Bitmap bm = nativeDecodeStream(is, tempStorage, outPadding, opts, Options.nativeInBitmap(opts), Options.nativeColorSpace(opts));
        if (bm == null) {
            return sMTKDcfDecoderManager.decodeDrmImageIfNeededImpl(tempStorage, is, opts);
        }
        return bm;
    }

    public static Bitmap decodeStream(InputStream is) {
        return decodeStream(is, null, null);
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd, Rect outPadding, Options opts) {
        Bitmap bm;
        Options.validate(opts);
        Trace.traceBegin(2, "decodeFileDescriptor");
        try {
            if (nativeIsSeekable(fd)) {
                bm = nativeDecodeFileDescriptor(fd, outPadding, opts, Options.nativeInBitmap(opts), Options.nativeColorSpace(opts));
            } else {
                FileInputStream fis = new FileInputStream(fd);
                try {
                    Bitmap bm2 = decodeStreamInternal(fis, outPadding, opts);
                    try {
                        fis.close();
                    } catch (Throwable th) {
                    }
                    bm = bm2;
                } catch (Throwable th2) {
                }
            }
            if (bm != null || opts == null || opts.inBitmap == null) {
                setDensityFromOptions(bm, opts);
                if (bm == null) {
                    return sMTKDcfDecoderManager.decodeDrmImageIfNeededImpl(fd, opts);
                }
                return bm;
            }
            throw new IllegalArgumentException("Problem decoding into existing bitmap");
            throw th;
        } finally {
            Trace.traceEnd(2);
        }
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd) {
        return decodeFileDescriptor(fd, null, null);
    }
}
