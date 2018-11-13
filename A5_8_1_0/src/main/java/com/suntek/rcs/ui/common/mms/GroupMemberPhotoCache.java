package com.suntek.rcs.ui.common.mms;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.ImageView;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Avatar;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.groupchat.GroupChatApi;
import com.suntek.mway.rcs.client.api.groupchat.GroupChatCallback;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class GroupMemberPhotoCache {
    private static final int IMAGE_PIXEL = 120;
    private static GroupMemberPhotoCache sInstance = new GroupMemberPhotoCache();
    private Handler mHandler;
    private HashMap<String, SoftReference<Bitmap>> mImageCache;
    private boolean mIsRuning;
    private LinkedBlockingQueue<LoaderImageTask> mTaskQueue;
    private Runnable runnable;

    public class LoaderImageTask {
        Bitmap bitmap;
        ImageView imageView;
        long mGroupId;
        String number;

        public LoaderImageTask(String number, ImageView imageView, long groupId) {
            this.number = number;
            this.imageView = imageView;
            this.mGroupId = groupId;
        }
    }

    private GroupMemberPhotoCache() {
        this.mIsRuning = false;
        this.runnable = new Runnable() {
            public void run() {
                while (GroupMemberPhotoCache.this.mIsRuning) {
                    try {
                        LoaderImageTask task = (LoaderImageTask) GroupMemberPhotoCache.this.mTaskQueue.take();
                        String number = (String) task.imageView.getTag();
                        if (!TextUtils.isEmpty(number) && number.equals(task.number)) {
                            GroupMemberPhotoCache.this.getbitmap(task);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                LoaderImageTask task = msg.obj;
                String number = (String) task.imageView.getTag();
                if (!TextUtils.isEmpty(number) && number.equals(task.number) && task.bitmap != null) {
                    task.imageView.setImageBitmap(task.bitmap);
                }
            }
        };
        this.mImageCache = new HashMap();
        this.mTaskQueue = new LinkedBlockingQueue();
        this.mIsRuning = true;
        new Thread(this.runnable).start();
    }

    public void loadGroupMemberPhoto(long rcsGroupId, String addr, ImageView mAvatar, Drawable defaultContactImage) {
        if (mAvatar != null) {
            mAvatar.setTag(addr);
            if (this.mImageCache.containsKey(addr)) {
                Bitmap bitmap = (Bitmap) ((SoftReference) this.mImageCache.get(addr)).get();
                if (bitmap == null) {
                    this.mImageCache.remove(addr);
                } else {
                    mAvatar.setImageBitmap(bitmap);
                    return;
                }
            }
            this.mTaskQueue.add(new LoaderImageTask(addr, mAvatar, rcsGroupId));
        }
    }

    public static GroupMemberPhotoCache getInstance() {
        if (sInstance == null) {
            sInstance = new GroupMemberPhotoCache();
        }
        return sInstance;
    }

    private void getbitmap(final LoaderImageTask task) {
        try {
            GroupChatApi.getInstance().getMemberAvatar(task.mGroupId, task.number, IMAGE_PIXEL, new GroupChatCallback() {
                public void onUpdateAvatar(Avatar avatar, int resultCode, String resultDesc) throws RemoteException {
                    if (avatar != null) {
                        String str = avatar.getImgBase64Str();
                        if (str != null) {
                            byte[] imageByte = Base64.decode(str, 0);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                            GroupMemberPhotoCache.this.addBitmapCache(task.number, bitmap);
                            task.bitmap = bitmap;
                            if (GroupMemberPhotoCache.this.mHandler != null) {
                                Message msg = GroupMemberPhotoCache.this.mHandler.obtainMessage();
                                msg.obj = task;
                                GroupMemberPhotoCache.this.mHandler.sendMessage(msg);
                            }
                        }
                    }
                }
            });
        } catch (ServiceDisconnectedException e) {
            e.printStackTrace();
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
    }

    public void removeCache(String number) {
        this.mImageCache.remove(number);
    }

    public void addBitmapCache(String number, Bitmap bitmap) {
        if (bitmap != null) {
            this.mImageCache.put(number, new SoftReference(bitmap));
        }
    }

    public Bitmap getBitmapByNumber(String number) {
        if (this.mImageCache == null || !this.mImageCache.containsKey(number)) {
            return null;
        }
        return (Bitmap) ((SoftReference) this.mImageCache.get(number)).get();
    }
}
