package com.mediatek.dcfDecoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.mediatek.dcfdecoder.DcfDecoder;
import java.io.FileDescriptor;
import java.io.InputStream;

public final class MTKDcfDecoderManagerImpl extends MTKDcfDecoderManager {
    public Bitmap decodeDrmImageIfNeededImpl(byte[] header, InputStream left, BitmapFactory.Options opts) {
        return DcfDecoder.decodeDrmImageIfNeeded(header, left, opts);
    }

    public Bitmap decodeDrmImageIfNeededImpl(FileDescriptor fd, BitmapFactory.Options opts) {
        return DcfDecoder.decodeDrmImageIfNeeded(fd, opts);
    }

    public Bitmap decodeDrmImageIfNeededImpl(byte[] data, BitmapFactory.Options opts) {
        return DcfDecoder.decodeDrmImageIfNeeded(data, opts);
    }
}
