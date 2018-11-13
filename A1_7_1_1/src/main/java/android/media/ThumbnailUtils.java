package android.media;

import android.app.backup.FullBackup;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.params.TonemapCurve;
import android.media.MediaFile.MediaFileType;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ThumbnailUtils {
    private static final boolean IS_DENSITY_XXXHIGH = false;
    private static final int MAX_NUM_PIXELS_MICRO_THUMBNAIL = 19200;
    private static final int MAX_NUM_PIXELS_THUMBNAIL = 0;
    private static final int OPTIONS_NONE = 0;
    public static final int OPTIONS_RECYCLE_INPUT = 2;
    private static final int OPTIONS_SCALE_UP = 1;
    private static final String TAG = "ThumbnailUtils";
    public static final int TARGET_SIZE_MICRO_THUMBNAIL = 96;
    public static final int TARGET_SIZE_MINI_THUMBNAIL = 0;
    private static final int UNCONSTRAINED = -1;

    private static class SizedThumbnailBitmap {
        public Bitmap mBitmap;
        public byte[] mThumbnailData;
        public int mThumbnailHeight;
        public int mThumbnailWidth;

        /* synthetic */ SizedThumbnailBitmap(SizedThumbnailBitmap sizedThumbnailBitmap) {
            this();
        }

        private SizedThumbnailBitmap() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.ThumbnailUtils.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.ThumbnailUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.ThumbnailUtils.<clinit>():void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00ff A:{SYNTHETIC, Splitter: B:62:0x00ff} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00e4 A:{SYNTHETIC, Splitter: B:54:0x00e4} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0111 A:{SYNTHETIC, Splitter: B:68:0x0111} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Bitmap createImageThumbnail(String filePath, int kind) {
        int targetSize;
        int maxPixels;
        IOException ex;
        OutOfMemoryError oom;
        Throwable th;
        boolean wantMini = kind == 1;
        if (wantMini) {
            targetSize = TARGET_SIZE_MINI_THUMBNAIL;
        } else {
            targetSize = 96;
        }
        if (wantMini) {
            maxPixels = MAX_NUM_PIXELS_THUMBNAIL;
        } else {
            maxPixels = MAX_NUM_PIXELS_MICRO_THUMBNAIL;
        }
        SizedThumbnailBitmap sizedThumbnailBitmap = new SizedThumbnailBitmap();
        Bitmap bitmap = null;
        MediaFileType fileType = MediaFile.getFileType(filePath);
        if (fileType != null && (fileType.fileType == 401 || MediaFile.isRawImageFileType(fileType.fileType))) {
            createThumbnailFromEXIF(filePath, targetSize, maxPixels, sizedThumbnailBitmap);
            bitmap = sizedThumbnailBitmap.mBitmap;
        }
        if (bitmap == null) {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(filePath);
                try {
                    FileDescriptor fd = stream2.getFD();
                    Options options = new Options();
                    options.inSampleSize = 1;
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFileDescriptor(fd, null, options);
                    if (!(options.mCancel || options.outWidth == -1)) {
                        if (options.outHeight != -1) {
                            options.inSampleSize = adustSampleSize(computeSampleSize(options, targetSize, maxPixels), options);
                            options.inJustDecodeBounds = false;
                            if (filePath.endsWith(".dcf")) {
                                options.inSampleSize |= 256;
                            }
                            options.inDither = false;
                            options.inPreferredConfig = Config.ARGB_8888;
                            bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
                            if (stream2 != null) {
                                try {
                                    stream2.close();
                                } catch (IOException ex2) {
                                    Log.e(TAG, "", ex2);
                                }
                            }
                        }
                    }
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException ex22) {
                            Log.e(TAG, "", ex22);
                        }
                    }
                    return null;
                } catch (IOException e) {
                    ex22 = e;
                    stream = stream2;
                    Log.e(TAG, "", ex22);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException ex222) {
                            Log.e(TAG, "", ex222);
                        }
                    }
                    if (kind == 3) {
                    }
                    return bitmap;
                } catch (OutOfMemoryError e2) {
                    oom = e2;
                    stream = stream2;
                    try {
                        Log.e(TAG, "Unable to decode file " + filePath + ". OutOfMemoryError.", oom);
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException ex2222) {
                                Log.e(TAG, "", ex2222);
                            }
                        }
                        if (kind == 3) {
                        }
                        return bitmap;
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    stream = stream2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException ex22222) {
                            Log.e(TAG, "", ex22222);
                        }
                    }
                    throw th;
                }
            } catch (IOException e3) {
                ex22222 = e3;
                Log.e(TAG, "", ex22222);
                if (stream != null) {
                }
                if (kind == 3) {
                }
                return bitmap;
            } catch (OutOfMemoryError e4) {
                oom = e4;
                Log.e(TAG, "Unable to decode file " + filePath + ". OutOfMemoryError.", oom);
                if (stream != null) {
                }
                if (kind == 3) {
                }
                return bitmap;
            }
        }
        if (kind == 3) {
            bitmap = extractThumbnail(bitmap, 96, 96, 2);
        }
        return bitmap;
    }

    public static Bitmap createVideoThumbnail(String filePath, int kind) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(-1);
            try {
                retriever.release();
            } catch (RuntimeException e) {
            }
        } catch (IllegalArgumentException e2) {
            try {
                retriever.release();
            } catch (RuntimeException e3) {
            }
        } catch (RuntimeException e4) {
            try {
                retriever.release();
            } catch (RuntimeException e5) {
            }
        } catch (Throwable th) {
            try {
                retriever.release();
            } catch (RuntimeException e6) {
            }
            throw th;
        }
        if (bitmap == null) {
            return null;
        }
        if (kind == 1) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > 512) {
                float scale = 512.0f / ((float) max);
                bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(((float) width) * scale), Math.round(((float) height) * scale), true);
            }
        } else if (kind == 3) {
            bitmap = extractThumbnail(bitmap, 96, 96, 2);
        }
        return bitmap;
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
        return extractThumbnail(source, width, height, 0);
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height, int options) {
        if (source == null) {
            return null;
        }
        float scale;
        Bitmap thumbnail;
        if (source.getWidth() < source.getHeight()) {
            scale = ((float) width) / ((float) source.getWidth());
        } else {
            scale = ((float) height) / ((float) source.getHeight());
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        RectF srcR = new RectF(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, (float) source.getWidth(), (float) source.getHeight());
        RectF deviceR = new RectF();
        matrix.mapRect(deviceR, srcR);
        if (deviceR.width() * deviceR.height() >= 1048576.0f) {
            thumbnail = transform(matrix, source, width, height, options);
        } else {
            thumbnail = transform(matrix, source, width, height, options | 1);
        }
        return thumbnail;
    }

    private static int computeSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        if (initialSize > 8) {
            return ((initialSize + 7) / 8) * 8;
        }
        int roundedSize = 1;
        while (roundedSize < initialSize) {
            roundedSize <<= 1;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int lowerBound;
        int upperBound;
        double w = (double) options.outWidth;
        double h = (double) options.outHeight;
        if (maxNumOfPixels == -1) {
            lowerBound = 1;
        } else {
            lowerBound = (int) Math.ceil(Math.sqrt((w * h) / ((double) maxNumOfPixels)));
        }
        if (minSideLength == -1) {
            upperBound = 128;
        } else {
            upperBound = (int) Math.min(Math.floor(w / ((double) minSideLength)), Math.floor(h / ((double) minSideLength)));
        }
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if (maxNumOfPixels == -1 && minSideLength == -1) {
            return 1;
        }
        if (minSideLength == -1) {
            return lowerBound;
        }
        return upperBound;
    }

    private static Bitmap makeBitmap(int minSideLength, int maxNumOfPixels, Uri uri, ContentResolver cr, ParcelFileDescriptor pfd, Options options) {
        if (pfd == null) {
            try {
                pfd = makeInputStream(uri, cr);
            } catch (OutOfMemoryError ex) {
                Log.e(TAG, "Got oom exception ", ex);
                return null;
            } finally {
                closeSilently(pfd);
            }
        }
        if (pfd == null) {
            closeSilently(pfd);
            return null;
        }
        if (options == null) {
            options = new Options();
        }
        FileDescriptor fd = pfd.getFileDescriptor();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        if (!(options.mCancel || options.outWidth == -1)) {
            if (options.outHeight != -1) {
                options.inSampleSize = computeSampleSize(options, minSideLength, maxNumOfPixels);
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Config.ARGB_8888;
                Bitmap b = BitmapFactory.decodeFileDescriptor(fd, null, options);
                closeSilently(pfd);
                return b;
            }
        }
        closeSilently(pfd);
        return null;
    }

    private static void closeSilently(ParcelFileDescriptor c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable th) {
            }
        }
    }

    private static ParcelFileDescriptor makeInputStream(Uri uri, ContentResolver cr) {
        try {
            return cr.openFileDescriptor(uri, FullBackup.ROOT_TREE_TOKEN);
        } catch (IOException e) {
            return null;
        }
    }

    private static Bitmap transform(Matrix scaler, Bitmap source, int targetWidth, int targetHeight, int options) {
        boolean scaleUp = (options & 1) != 0;
        boolean recycle = (options & 2) != 0;
        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        Bitmap b2;
        if (scaleUp || (deltaX >= 0 && deltaY >= 0)) {
            Bitmap b1;
            float bitmapWidthF = (float) source.getWidth();
            float bitmapHeightF = (float) source.getHeight();
            float scale;
            if (bitmapWidthF / bitmapHeightF > ((float) targetWidth) / ((float) targetHeight)) {
                scale = ((float) targetHeight) / bitmapHeightF;
                if (scale < 0.9f || scale > 1.0f) {
                    scaler.setScale(scale, scale);
                } else {
                    scaler = null;
                }
            } else {
                scale = ((float) targetWidth) / bitmapWidthF;
                if (scale < 0.9f || scale > 1.0f) {
                    scaler.setScale(scale, scale);
                } else {
                    scaler = null;
                }
            }
            if (scaler != null) {
                b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
            } else {
                b1 = source;
            }
            if (recycle && b1 != source) {
                source.recycle();
            }
            b2 = Bitmap.createBitmap(b1, Math.max(0, b1.getWidth() - targetWidth) / 2, Math.max(0, b1.getHeight() - targetHeight) / 2, targetWidth, targetHeight);
            if (b2 != b1 && (recycle || b1 != source)) {
                b1.recycle();
            }
            return b2;
        }
        b2 = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
        Canvas c = new Canvas(b2);
        int deltaXHalf = Math.max(0, deltaX / 2);
        int deltaYHalf = Math.max(0, deltaY / 2);
        Rect rect = new Rect(deltaXHalf, deltaYHalf, Math.min(targetWidth, source.getWidth()) + deltaXHalf, Math.min(targetHeight, source.getHeight()) + deltaYHalf);
        int dstX = (targetWidth - rect.width()) / 2;
        int dstY = (targetHeight - rect.height()) / 2;
        c.drawBitmap(source, rect, new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY), null);
        if (recycle) {
            source.recycle();
        }
        c.setBitmap(null);
        return b2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x001d  */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x001d  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0040  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void createThumbnailFromEXIF(String filePath, int targetSize, int maxPixels, SizedThumbnailBitmap sizedThumbBitmap) {
        IOException ex;
        Options fullOptions;
        int exifThumbWidth;
        if (filePath != null) {
            Options exifOptions;
            byte[] thumbData = null;
            try {
                ExifInterface exif = new ExifInterface(filePath);
                try {
                    thumbData = exif.getThumbnail();
                    ExifInterface exifInterface = exif;
                } catch (IOException e) {
                    ex = e;
                    Log.w(TAG, "", ex);
                    fullOptions = new Options();
                    exifOptions = new Options();
                    exifThumbWidth = 0;
                    if (thumbData != null) {
                    }
                    fullOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, fullOptions);
                    if (fullOptions.outWidth != -1) {
                    }
                    sizedThumbBitmap.mBitmap = null;
                    return;
                }
            } catch (IOException e2) {
                ex = e2;
                Log.w(TAG, "", ex);
                fullOptions = new Options();
                exifOptions = new Options();
                exifThumbWidth = 0;
                if (thumbData != null) {
                }
                fullOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, fullOptions);
                if (fullOptions.outWidth != -1) {
                }
                sizedThumbBitmap.mBitmap = null;
                return;
            }
            fullOptions = new Options();
            exifOptions = new Options();
            exifThumbWidth = 0;
            if (thumbData != null) {
                exifOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, exifOptions);
                exifOptions.inSampleSize = adustSampleSize(computeSampleSize(exifOptions, targetSize, maxPixels), exifOptions);
                exifThumbWidth = exifOptions.outWidth / exifOptions.inSampleSize;
            }
            fullOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, fullOptions);
            if (fullOptions.outWidth != -1 || fullOptions.outHeight == -1) {
                sizedThumbBitmap.mBitmap = null;
                return;
            }
            fullOptions.inSampleSize = adustSampleSize(computeSampleSize(fullOptions, targetSize, maxPixels), fullOptions);
            int fullThumbWidth = fullOptions.outWidth / fullOptions.inSampleSize;
            if (thumbData == null || exifThumbWidth < fullThumbWidth) {
                fullOptions.inJustDecodeBounds = false;
                sizedThumbBitmap.mBitmap = BitmapFactory.decodeFile(filePath, fullOptions);
            } else {
                int width = exifOptions.outWidth;
                int height = exifOptions.outHeight;
                exifOptions.inJustDecodeBounds = false;
                sizedThumbBitmap.mBitmap = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, exifOptions);
                if (sizedThumbBitmap.mBitmap != null) {
                    sizedThumbBitmap.mThumbnailData = thumbData;
                    sizedThumbBitmap.mThumbnailWidth = width;
                    sizedThumbBitmap.mThumbnailHeight = height;
                }
            }
        }
    }

    public static Bitmap extractBufferThumbnail(byte[] source, int srcWidth, int srcHeight, int dstWidth, int dstHeight, int options) {
        if (source == null) {
            return null;
        }
        float scale;
        Bitmap thumbnail;
        if (srcWidth < srcHeight) {
            scale = ((float) dstWidth) / ((float) srcWidth);
        } else {
            scale = ((float) dstHeight) / ((float) srcHeight);
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        RectF srcR = new RectF(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, (float) srcWidth, (float) srcHeight);
        RectF deviceR = new RectF();
        matrix.mapRect(deviceR, srcR);
        if (deviceR.width() * deviceR.height() >= 1048576.0f) {
            thumbnail = transformBuffer(matrix, source, srcWidth, srcHeight, dstWidth, dstHeight, options);
        } else {
            thumbnail = transformBuffer(matrix, source, srcWidth, srcHeight, dstWidth, dstHeight, options | 1);
        }
        return thumbnail;
    }

    private static Bitmap transformBuffer(Matrix scaler, byte[] source, int srcWidth, int srcHeight, int targetWidth, int targetHeight, int options) {
        int deltaX = srcWidth - targetWidth;
        int deltaY = srcHeight - targetHeight;
        Bitmap b1;
        Options Options;
        Bitmap b2;
        Canvas c;
        Rect rect;
        if (((options & 1) != 0) || (deltaX >= 0 && deltaY >= 0)) {
            float bitmapWidthF = (float) srcWidth;
            float bitmapHeightF = (float) srcHeight;
            float finalScale = 1.0f;
            float scale;
            if (bitmapWidthF / bitmapHeightF > ((float) targetWidth) / ((float) targetHeight)) {
                scale = ((float) targetHeight) / bitmapHeightF;
                if (scale < 0.9f || scale > 1.0f) {
                    scaler.setScale(scale, scale);
                    finalScale = scale;
                }
            } else {
                scale = ((float) targetWidth) / bitmapWidthF;
                if (scale < 0.9f || scale > 1.0f) {
                    scaler.setScale(scale, scale);
                    finalScale = scale;
                }
            }
            b1 = null;
            Options = new Options();
            Options.inDither = false;
            Options.inPreferredConfig = Config.ARGB_8888;
            int inPreferSize = (int) Math.max(((float) srcWidth) * finalScale, ((float) srcHeight) * finalScale);
            if (source != null) {
                b1 = BitmapFactory.decodeByteArray(source, 0, source.length, Options);
            }
            if (b1 == null) {
                return null;
            }
            int maxSize;
            int scaledBitmapWidth = b1.getWidth();
            int scaledBitmapHeight = b1.getHeight();
            b2 = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
            c = new Canvas(b2);
            RectF rectF = new RectF(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, (float) targetWidth, (float) targetHeight);
            if (scaledBitmapWidth > scaledBitmapHeight) {
                maxSize = scaledBitmapWidth;
            } else {
                maxSize = scaledBitmapHeight;
            }
            int croppedX;
            int croppedY;
            if (maxSize == inPreferSize) {
                croppedX = Math.max(0, (scaledBitmapWidth - targetWidth) / 2);
                croppedY = Math.max(0, (scaledBitmapHeight - targetHeight) / 2);
                rect = new Rect(croppedX, croppedY, Math.min(croppedX + targetWidth, scaledBitmapWidth), Math.min(croppedY + targetHeight, scaledBitmapHeight));
            } else {
                croppedX = Math.max(0, (srcWidth - ((int) (((float) targetWidth) / finalScale))) / 2);
                croppedY = Math.max(0, (srcHeight - ((int) (((float) targetHeight) / finalScale))) / 2);
                rect = new Rect(croppedX, croppedY, Math.min(((int) (((float) targetWidth) / finalScale)) + croppedX, srcWidth), Math.min(((int) (((float) targetHeight) / finalScale)) + croppedY, srcHeight));
            }
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            c.drawBitmap(b1, src, rectF, paint);
            b1.recycle();
            return b2;
        }
        b2 = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
        c = new Canvas(b2);
        int deltaXHalf = Math.max(0, deltaX / 2);
        int deltaYHalf = Math.max(0, deltaY / 2);
        rect = new Rect(deltaXHalf, deltaYHalf, Math.min(targetWidth, srcWidth) + deltaXHalf, Math.min(targetHeight, srcHeight) + deltaYHalf);
        int dstX = (targetWidth - rect.width()) / 2;
        int dstY = (targetHeight - rect.height()) / 2;
        rect = new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY);
        Options = new Options();
        Options.inDither = false;
        Options.inSampleSize = 1;
        Options.inPreferredConfig = Config.ARGB_8888;
        b1 = null;
        if (source != null) {
            b1 = BitmapFactory.decodeByteArray(source, 0, source.length, Options);
        }
        if (b1 == null) {
            return null;
        }
        c.drawBitmap(b1, rect, rect, null);
        b1.recycle();
        return b2;
    }

    private static int adustSampleSize(int inSampleSize, Options options) {
        if (inSampleSize < 1 || options == null) {
            return 1;
        }
        int imageShortterDimension = options.outWidth < options.outHeight ? options.outWidth : options.outHeight;
        while (inSampleSize > 1 && imageShortterDimension / inSampleSize < 96) {
            inSampleSize >>= 1;
        }
        return inSampleSize;
    }
}
