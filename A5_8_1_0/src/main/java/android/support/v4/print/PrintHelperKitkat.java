package android.support.v4.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument.Page;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintAttributes.MediaSize;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentAdapter.LayoutResultCallback;
import android.print.PrintDocumentAdapter.WriteResultCallback;
import android.print.PrintDocumentInfo.Builder;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class PrintHelperKitkat {
    public static final int COLOR_MODE_COLOR = 2;
    public static final int COLOR_MODE_MONOCHROME = 1;
    private static final String LOG_TAG = "PrintHelperKitkat";
    private static final int MAX_PRINT_SIZE = 3500;
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;
    public static final int SCALE_MODE_FILL = 2;
    public static final int SCALE_MODE_FIT = 1;
    int mColorMode = 2;
    final Context mContext;
    Options mDecodeOptions = null;
    private final Object mLock = new Object();
    int mOrientation = 1;
    int mScaleMode = 2;

    public interface OnPrintFinishCallback {
        void onFinish();
    }

    PrintHelperKitkat(Context context) {
        this.mContext = context;
    }

    public void setScaleMode(int scaleMode) {
        this.mScaleMode = scaleMode;
    }

    public int getScaleMode() {
        return this.mScaleMode;
    }

    public void setColorMode(int colorMode) {
        this.mColorMode = colorMode;
    }

    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public int getColorMode() {
        return this.mColorMode;
    }

    public void printBitmap(String jobName, Bitmap bitmap, OnPrintFinishCallback callback) {
        if (bitmap != null) {
            final int fittingMode = this.mScaleMode;
            PrintManager printManager = (PrintManager) this.mContext.getSystemService("print");
            MediaSize mediaSize = MediaSize.UNKNOWN_PORTRAIT;
            if (bitmap.getWidth() > bitmap.getHeight()) {
                mediaSize = MediaSize.UNKNOWN_LANDSCAPE;
            }
            final String str = jobName;
            final Bitmap bitmap2 = bitmap;
            final OnPrintFinishCallback onPrintFinishCallback = callback;
            printManager.print(jobName, new PrintDocumentAdapter() {
                private PrintAttributes mAttributes;

                public void onLayout(PrintAttributes oldPrintAttributes, PrintAttributes newPrintAttributes, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
                    this.mAttributes = newPrintAttributes;
                    layoutResultCallback.onLayoutFinished(new Builder(str).setContentType(1).setPageCount(1).build(), newPrintAttributes.equals(oldPrintAttributes) ^ 1);
                }

                public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor fileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
                    PrintedPdfDocument pdfDocument = new PrintedPdfDocument(PrintHelperKitkat.this.mContext, this.mAttributes);
                    try {
                        Page page = pdfDocument.startPage(1);
                        page.getCanvas().drawBitmap(bitmap2, PrintHelperKitkat.this.getMatrix(bitmap2.getWidth(), bitmap2.getHeight(), new RectF(page.getInfo().getContentRect()), fittingMode), null);
                        pdfDocument.finishPage(page);
                        pdfDocument.writeTo(new FileOutputStream(fileDescriptor.getFileDescriptor()));
                        writeResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                    } catch (IOException ioe) {
                        Log.e(PrintHelperKitkat.LOG_TAG, "Error writing printed content", ioe);
                        writeResultCallback.onWriteFailed(null);
                    } catch (Throwable th) {
                        if (pdfDocument != null) {
                            pdfDocument.close();
                        }
                        if (fileDescriptor != null) {
                            try {
                                fileDescriptor.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                    if (pdfDocument != null) {
                        pdfDocument.close();
                    }
                    if (fileDescriptor != null) {
                        try {
                            fileDescriptor.close();
                        } catch (IOException e2) {
                        }
                    }
                }

                public void onFinish() {
                    if (onPrintFinishCallback != null) {
                        onPrintFinishCallback.onFinish();
                    }
                }
            }, new PrintAttributes.Builder().setMediaSize(mediaSize).setColorMode(this.mColorMode).build());
        }
    }

    private Matrix getMatrix(int imageWidth, int imageHeight, RectF content, int fittingMode) {
        Matrix matrix = new Matrix();
        float scale = content.width() / ((float) imageWidth);
        if (fittingMode == 2) {
            scale = Math.max(scale, content.height() / ((float) imageHeight));
        } else {
            scale = Math.min(scale, content.height() / ((float) imageHeight));
        }
        matrix.postScale(scale, scale);
        matrix.postTranslate((content.width() - (((float) imageWidth) * scale)) / 2.0f, (content.height() - (((float) imageHeight) * scale)) / 2.0f);
        return matrix;
    }

    public void printBitmap(String jobName, Uri imageFile, OnPrintFinishCallback callback) throws FileNotFoundException {
        final int fittingMode = this.mScaleMode;
        final String str = jobName;
        final OnPrintFinishCallback onPrintFinishCallback = callback;
        final Uri uri = imageFile;
        PrintDocumentAdapter printDocumentAdapter = new PrintDocumentAdapter() {
            AsyncTask<Uri, Boolean, Bitmap> loadBitmap;
            private PrintAttributes mAttributes;
            Bitmap mBitmap = null;

            public void onLayout(PrintAttributes oldPrintAttributes, PrintAttributes newPrintAttributes, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
                if (cancellationSignal.isCanceled()) {
                    layoutResultCallback.onLayoutCancelled();
                    this.mAttributes = newPrintAttributes;
                } else if (this.mBitmap != null) {
                    layoutResultCallback.onLayoutFinished(new Builder(str).setContentType(1).setPageCount(1).build(), newPrintAttributes.equals(oldPrintAttributes) ^ 1);
                } else {
                    final Uri uri = uri;
                    final String str = str;
                    final CancellationSignal cancellationSignal2 = cancellationSignal;
                    final PrintAttributes printAttributes = newPrintAttributes;
                    final PrintAttributes printAttributes2 = oldPrintAttributes;
                    final LayoutResultCallback layoutResultCallback2 = layoutResultCallback;
                    this.loadBitmap = new AsyncTask<Uri, Boolean, Bitmap>() {
                        protected void onPreExecute() {
                            cancellationSignal2.setOnCancelListener(new OnCancelListener() {
                                public void onCancel() {
                                    AnonymousClass2.this.cancelLoad();
                                    AnonymousClass1.this.cancel(false);
                                }
                            });
                        }

                        protected Bitmap doInBackground(Uri... uris) {
                            try {
                                return PrintHelperKitkat.this.loadConstrainedBitmap(uri, PrintHelperKitkat.MAX_PRINT_SIZE);
                            } catch (FileNotFoundException e) {
                                return null;
                            }
                        }

                        protected void onPostExecute(Bitmap bitmap) {
                            super.onPostExecute(bitmap);
                            AnonymousClass2.this.mBitmap = bitmap;
                            if (bitmap != null) {
                                layoutResultCallback2.onLayoutFinished(new Builder(str).setContentType(1).setPageCount(1).build(), printAttributes.equals(printAttributes2) ^ 1);
                                return;
                            }
                            layoutResultCallback2.onLayoutFailed(null);
                        }

                        protected void onCancelled(Bitmap result) {
                            layoutResultCallback2.onLayoutCancelled();
                        }
                    };
                    this.loadBitmap.execute(new Uri[0]);
                    this.mAttributes = newPrintAttributes;
                }
            }

            private void cancelLoad() {
                synchronized (PrintHelperKitkat.this.mLock) {
                    if (PrintHelperKitkat.this.mDecodeOptions != null) {
                        PrintHelperKitkat.this.mDecodeOptions.requestCancelDecode();
                        PrintHelperKitkat.this.mDecodeOptions = null;
                    }
                }
            }

            public void onFinish() {
                super.onFinish();
                cancelLoad();
                this.loadBitmap.cancel(true);
                if (onPrintFinishCallback != null) {
                    onPrintFinishCallback.onFinish();
                }
            }

            public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor fileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
                PrintedPdfDocument pdfDocument = new PrintedPdfDocument(PrintHelperKitkat.this.mContext, this.mAttributes);
                try {
                    Page page = pdfDocument.startPage(1);
                    page.getCanvas().drawBitmap(this.mBitmap, PrintHelperKitkat.this.getMatrix(this.mBitmap.getWidth(), this.mBitmap.getHeight(), new RectF(page.getInfo().getContentRect()), fittingMode), null);
                    pdfDocument.finishPage(page);
                    pdfDocument.writeTo(new FileOutputStream(fileDescriptor.getFileDescriptor()));
                    writeResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                } catch (IOException ioe) {
                    Log.e(PrintHelperKitkat.LOG_TAG, "Error writing printed content", ioe);
                    writeResultCallback.onWriteFailed(null);
                } catch (Throwable th) {
                    if (pdfDocument != null) {
                        pdfDocument.close();
                    }
                    if (fileDescriptor != null) {
                        try {
                            fileDescriptor.close();
                        } catch (IOException e) {
                        }
                    }
                }
                if (pdfDocument != null) {
                    pdfDocument.close();
                }
                if (fileDescriptor != null) {
                    try {
                        fileDescriptor.close();
                    } catch (IOException e2) {
                    }
                }
            }
        };
        PrintManager printManager = (PrintManager) this.mContext.getSystemService("print");
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setColorMode(this.mColorMode);
        if (this.mOrientation == 1) {
            builder.setMediaSize(MediaSize.UNKNOWN_LANDSCAPE);
        } else if (this.mOrientation == 2) {
            builder.setMediaSize(MediaSize.UNKNOWN_PORTRAIT);
        }
        printManager.print(jobName, printDocumentAdapter, builder.build());
    }

    private Bitmap loadConstrainedBitmap(Uri uri, int maxSideLength) throws FileNotFoundException {
        if (maxSideLength <= 0 || uri == null || this.mContext == null) {
            throw new IllegalArgumentException("bad argument to getScaledBitmap");
        }
        Options opt = new Options();
        opt.inJustDecodeBounds = true;
        loadBitmap(uri, opt);
        int w = opt.outWidth;
        int h = opt.outHeight;
        if (w <= 0 || h <= 0) {
            return null;
        }
        int imageSide = Math.max(w, h);
        int sampleSize = 1;
        while (imageSide > maxSideLength) {
            imageSide >>>= 1;
            sampleSize <<= 1;
        }
        if (sampleSize <= 0 || Math.min(w, h) / sampleSize <= 0) {
            return null;
        }
        Options decodeOptions;
        synchronized (this.mLock) {
            this.mDecodeOptions = new Options();
            this.mDecodeOptions.inMutable = true;
            this.mDecodeOptions.inSampleSize = sampleSize;
            decodeOptions = this.mDecodeOptions;
        }
        try {
            Bitmap loadBitmap = loadBitmap(uri, decodeOptions);
            synchronized (this.mLock) {
                this.mDecodeOptions = null;
            }
            return loadBitmap;
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mDecodeOptions = null;
            }
        }
    }

    private Bitmap loadBitmap(Uri uri, Options o) throws FileNotFoundException {
        if (uri == null || this.mContext == null) {
            throw new IllegalArgumentException("bad argument to loadBitmap");
        }
        InputStream is = null;
        try {
            is = this.mContext.getContentResolver().openInputStream(uri);
            Bitmap decodeStream = BitmapFactory.decodeStream(is, null, o);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException t) {
                    Log.w(LOG_TAG, "close fail ", t);
                }
            }
            return decodeStream;
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException t2) {
                    Log.w(LOG_TAG, "close fail ", t2);
                }
            }
        }
    }
}
