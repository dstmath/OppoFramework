package com.suntek.rcs.ui.common.mms;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Base64;
import android.widget.ImageView;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Avatar;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Profile;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.QRCardInfo;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.profile.ProfileApi;
import com.suntek.mway.rcs.client.api.profile.ProfileListener;
import com.suntek.rcs.ui.common.RcsLog;
import java.lang.ref.SoftReference;
import java.util.HashMap;

public class RcsMyProfileCache {
    private static final int IMAGE_PIXEL = 120;
    private static RcsMyProfileCache sInstance;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            RcsLog.i("handler receiver message");
            RcsMyProfileCache.this.mImageCache = new HashMap();
            Bitmap bitmap = msg.obj;
            RcsMyProfileCache.this.mImageView.setImageBitmap(bitmap);
            if (bitmap != null) {
                RcsMyProfileCache.this.addBitmapCache("me", bitmap);
            }
        }
    };
    private HashMap<String, SoftReference<Bitmap>> mImageCache;
    private ImageView mImageView;
    private boolean mIsRuning = false;
    private Runnable runnable = new Runnable() {
        public void run() {
            RcsMyProfileCache.this.getbitmap();
        }
    };

    private RcsMyProfileCache(ImageView imageView) {
        RcsLog.i("new RcsMyProfileCache");
        this.mImageView = imageView;
        new Thread(this.runnable).start();
    }

    public Bitmap getMyHeadPic() {
        if (this.mImageCache != null) {
            return (Bitmap) ((SoftReference) this.mImageCache.get("me")).get();
        }
        return null;
    }

    public static RcsMyProfileCache getInstance(ImageView photoView) {
        if (sInstance == null) {
            sInstance = new RcsMyProfileCache(photoView);
        }
        return sInstance;
    }

    private void getbitmap() {
        try {
            ProfileApi.getInstance().getMyHeadPic(new ProfileListener() {
                public void onAvatarGet(Avatar photo, int resultCode, String resultDesc) throws RemoteException {
                    RcsLog.i("RcsMyProfileCache get my photo from service");
                    if (photo != null) {
                        String str = photo.getImgBase64Str();
                        if (str != null) {
                            byte[] imageByte = Base64.decode(str, 0);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                            if (RcsMyProfileCache.this.mHandler != null) {
                                Message msg = new Message();
                                msg.obj = bitmap;
                                RcsMyProfileCache.this.mHandler.sendMessage(msg);
                            }
                        }
                    }
                }

                public void onAvatarUpdated(int arg0, String arg1) throws RemoteException {
                }

                public void onProfileGet(Profile arg0, int arg1, String arg2) throws RemoteException {
                }

                public void onProfileUpdated(int arg0, String arg1) throws RemoteException {
                }

                public void onQRImgDecode(QRCardInfo imgObj, int resultCode, String arg2) throws RemoteException {
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
            RcsLog.i("RcsMyProfileCache add bitmap to cache, number=" + number);
            this.mImageCache.put(number, new SoftReference(bitmap));
        }
    }
}
