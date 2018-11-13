package com.suntek.rcs.ui.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageLoader {
    private static final int MIN_MEMORY = 10485760;
    private static LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(Math.max(((int) Runtime.getRuntime().maxMemory()) / 8, MIN_MEMORY)) {
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    };
    private Context context;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private HashMap<String, Future> futureMap = new HashMap();
    private Handler handler;

    public ImageLoader(Context context) {
        this.context = context;
        this.handler = new Handler();
    }

    public void load(ImageView imageView, String path, int default_id, final int fail_id) {
        if (imageView != null) {
            if (TextUtils.isEmpty(path) || path.equals("null")) {
                imageView.setImageResource(default_id);
                return;
            }
            boolean isNetImage = false;
            if (path.startsWith("http://") || path.startsWith("https://")) {
                String realPath = getLocalFilePath(path);
                if (realPath.equals(path)) {
                    isNetImage = true;
                } else {
                    path = realPath;
                }
            }
            final String uri = path;
            Bitmap bitmap = (Bitmap) bitmapCache.get(path);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
            ImageGetter imageGetter;
            final ImageTask imageTask = new ImageTask(uri, imageView, default_id, fail_id);
            ImageLoaderListener listener = new ImageLoaderListener() {
                public void onLoaded(String url, final Bitmap bitmap, final ImageView imageView) {
                    if (bitmap == null) {
                        imageView.setImageResource(fail_id);
                        return;
                    }
                    if (!(imageView.getTag() == null || (imageTask.isCanceled() ^ 1) == 0 || !String.valueOf(imageView.getTag()).equals(url))) {
                        ImageLoader.this.handler.post(new Runnable() {
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                    ImageLoader.bitmapCache.put(uri, bitmap);
                    NetImageUtil.saveBitmap(ImageLoader.this.context, uri, bitmap);
                }

                public boolean onStartLoad() {
                    return imageTask.isCanceled() ^ 1;
                }

                public void onEndLoad() {
                    ImageLoader.this.futureMap.remove(uri);
                }
            };
            if (isNetImage) {
                imageGetter = new NetImageGetter(imageTask, listener);
            } else {
                imageGetter = new FileImageGetter(imageTask, listener);
            }
            imageView.setTag(uri);
            imageView.setImageResource(default_id);
            this.futureMap.put(uri, this.executor.submit(imageGetter));
        }
    }

    public void cancel(String path) {
        if (!TextUtils.isEmpty(path)) {
            path = getLocalFilePath(path);
            Future future = (Future) this.futureMap.get(path);
            if (future != null) {
                future.cancel(true);
                this.futureMap.remove(path);
            }
        }
    }

    private String getLocalFilePath(String path) {
        String filePath = NetImageUtil.getImgDownloadPath(this.context) + NetImageUtil.getImgNameByUrl(path);
        if (new File(filePath).exists()) {
            return filePath;
        }
        return path;
    }

    public void destroy() {
        this.executor.shutdown();
        bitmapCache.evictAll();
        this.futureMap.clear();
        this.context = null;
    }
}
