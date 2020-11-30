package com.mediatek.dcfDecoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.FileDescriptor;
import java.io.InputStream;

public class MTKDcfDecoderManager {
    public Bitmap decodeDrmImageIfNeededImpl(byte[] header, InputStream left, BitmapFactory.Options opts) {
        return null;
    }

    public Bitmap decodeDrmImageIfNeededImpl(FileDescriptor fd, BitmapFactory.Options opts) {
        return null;
    }

    public Bitmap decodeDrmImageIfNeededImpl(byte[] data, BitmapFactory.Options opts) {
        return null;
    }
}
