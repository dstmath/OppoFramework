package com.mediatek.internal.telephony.cat;

import android.os.AsyncResult;
import android.os.Looper;
import android.os.Message;
import com.android.internal.telephony.cat.IconLoader;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.util.HexDump;

public class MtkIconLoader extends IconLoader {
    private static final String TAG = "MtkIconLoader";

    public MtkIconLoader(Looper looper, IccFileHandler fh) {
        super(looper, fh);
    }

    public void handleMessage(Message msg) {
        try {
            int i = msg.what;
            if (i == 1) {
                MtkCatLog.d(TAG, "load EFimg done");
                if (msg.obj == null) {
                    MtkCatLog.e(TAG, "msg.obj is null.");
                    return;
                }
                MtkCatLog.d(TAG, "msg.obj is " + msg.obj.getClass().getName());
                AsyncResult ar = (AsyncResult) msg.obj;
                MtkCatLog.d(TAG, "EFimg raw data: " + HexDump.toHexString((byte[]) ar.result));
                if (handleImageDescriptor((byte[]) ar.result)) {
                    readIconData();
                    return;
                }
                throw new Exception("Unable to parse image descriptor");
            } else if (i != 2) {
                MtkIconLoader.super.handleMessage(msg);
            } else {
                MtkCatLog.d(TAG, "load icon done");
                byte[] rawData = (byte[]) ((AsyncResult) msg.obj).result;
                MtkCatLog.d(TAG, "icon raw data: " + HexDump.toHexString(rawData));
                MtkCatLog.d(TAG, "load icon CODING_SCHEME = " + this.mId.mCodingScheme);
                if (this.mId.mCodingScheme == 17) {
                    this.mCurrentIcon = parseToBnW(rawData, rawData.length);
                    this.mIconsCache.put(Integer.valueOf(this.mRecordNumber), this.mCurrentIcon);
                    postIcon();
                } else if (this.mId.mCodingScheme == 33) {
                    this.mIconData = rawData;
                    readClut();
                } else {
                    MtkCatLog.d(TAG, "else  /postIcon ");
                    postIcon();
                }
            }
        } catch (Exception e) {
            MtkCatLog.d(this, "Icon load failed");
            e.printStackTrace();
            postIcon();
        }
    }
}
