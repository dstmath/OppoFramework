package com.suntek.rcs.ui.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetImageGetter extends ImageGetter {
    public NetImageGetter(ImageTask imageTask, ImageLoaderListener listener) {
        super(imageTask, listener);
    }

    public void loadImage(String path) {
        this.imageTask.setLoading(true);
        this.listener.onLoaded(path, getHttpBitmap(path), this.imageTask.getImageView());
        this.imageTask.setLoading(false);
    }

    public Bitmap getHttpBitmap(String path) {
        Bitmap bitmap = null;
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            connection = (HttpURLConnection) new URL(path).openConnection();
            connection.setConnectTimeout(5000);
            connection.setDoInput(true);
            connection.connect();
            is = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            try {
                is.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            try {
                is.close();
                connection.disconnect();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        } catch (Throwable th) {
            try {
                is.close();
                connection.disconnect();
            } catch (IOException e32) {
                e32.printStackTrace();
            }
            throw th;
        }
        return bitmap;
    }
}
