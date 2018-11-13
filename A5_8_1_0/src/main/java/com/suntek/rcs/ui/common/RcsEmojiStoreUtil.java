package com.suntek.rcs.ui.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.ImageView;
import com.suntek.mway.rcs.client.api.emoticon.EmoticonApi;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class RcsEmojiStoreUtil {
    public static final int EMO_DYNAMIC_FILE = 2;
    public static final int EMO_PACKAGE_FILE = 3;
    public static final int EMO_STATIC_FILE = 1;
    private static RcsEmojiStoreUtil mInstance;
    private Map<String, SoftReference<Bitmap>> mCaches;
    private Handler mHandler;
    private boolean mIsRuning;
    private LinkedBlockingQueue<LoaderImageTask> mTaskQueue;
    private Runnable runnable;

    public class LoaderImageTask {
        Bitmap bitmap;
        String imageId;
        ImageView imageView;
        int loaderType;

        public LoaderImageTask(String imageId, ImageView imageView, int loaderType) {
            this.imageId = imageId;
            this.imageView = imageView;
            this.loaderType = loaderType;
        }
    }

    public static RcsEmojiStoreUtil getInstance() {
        if (mInstance == null) {
            mInstance = new RcsEmojiStoreUtil();
        }
        return mInstance;
    }

    private RcsEmojiStoreUtil() {
        this.mIsRuning = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                LoaderImageTask task = msg.obj;
                String imageId = (String) task.imageView.getTag();
                if (!TextUtils.isEmpty(imageId) && imageId.equals(task.imageId) && task.bitmap != null) {
                    task.imageView.setImageBitmap(task.bitmap);
                }
            }
        };
        this.runnable = new Runnable() {
            public void run() {
                while (RcsEmojiStoreUtil.this.mIsRuning) {
                    try {
                        LoaderImageTask task = (LoaderImageTask) RcsEmojiStoreUtil.this.mTaskQueue.take();
                        String imageId = (String) task.imageView.getTag();
                        if (!TextUtils.isEmpty(imageId) && imageId.equals(task.imageId)) {
                            task.bitmap = RcsEmojiStoreUtil.this.getbitmap(task.loaderType, task.imageId);
                            if (RcsEmojiStoreUtil.this.mHandler != null) {
                                Message msg = RcsEmojiStoreUtil.this.mHandler.obtainMessage();
                                msg.obj = task;
                                RcsEmojiStoreUtil.this.mHandler.sendMessage(msg);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        this.mCaches = new HashMap();
        this.mTaskQueue = new LinkedBlockingQueue();
        this.mIsRuning = true;
        new Thread(this.runnable).start();
    }

    public void loadImageAsynById(ImageView imageView, String imageId, int loaderType) {
        if (imageView != null) {
            imageView.setTag(imageId);
            if (this.mCaches.containsKey(imageId)) {
                Bitmap bitmap = (Bitmap) ((SoftReference) this.mCaches.get(imageId)).get();
                if (bitmap == null) {
                    this.mCaches.remove(imageId);
                } else {
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            }
            this.mTaskQueue.add(new LoaderImageTask(imageId, imageView, loaderType));
        }
    }

    private Bitmap getbitmap(int loaderType, String emoticonId) {
        byte[] imageByte = null;
        Bitmap bitmap = null;
        if (loaderType == 1) {
            try {
                imageByte = EmoticonApi.getInstance().decrypt2Bytes(emoticonId, 1);
            } catch (ServiceDisconnectedException e) {
                e.printStackTrace();
            } catch (RemoteException e2) {
                e2.printStackTrace();
            }
        } else if (loaderType == 3) {
            imageByte = EmoticonApi.getInstance().decrypt2Bytes(emoticonId, 3);
        }
        if (imageByte != null) {
            bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
        }
        if (bitmap != null) {
            this.mCaches.put(emoticonId, new SoftReference(bitmap));
        }
        return bitmap;
    }
}
