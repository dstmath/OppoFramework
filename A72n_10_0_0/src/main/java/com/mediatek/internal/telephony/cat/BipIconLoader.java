package com.mediatek.internal.telephony.cat;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.cat.ImageDescriptor;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.util.HexDump;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import java.util.HashMap;

class BipIconLoader extends Handler {
    private static final int CLUT_ENTRY_SIZE = 3;
    private static final int CLUT_LOCATION_OFFSET = 4;
    private static final int EVENT_READ_CLUT_DONE = 3;
    private static final int EVENT_READ_EF_IMG_RECOED_DONE = 1;
    private static final int EVENT_READ_ICON_DONE = 2;
    private static final int STATE_MULTI_ICONS = 2;
    private static final int STATE_SINGLE_ICON = 1;
    private static final String TAG = "Stk-BipIL";
    private static BipIconLoader[] sLoader = null;
    private static int sSimCount = 0;
    private static HandlerThread[] sThread = null;
    private Bitmap mCurrentIcon = null;
    private int mCurrentRecordIndex = 0;
    private Message mEndMsg = null;
    private byte[] mIconData = null;
    private Bitmap[] mIcons = null;
    private HashMap<Integer, Bitmap> mIconsCache = null;
    private ImageDescriptor mId = null;
    private int mRecordNumber;
    private int[] mRecordNumbers = null;
    private IccFileHandler mSimFH = null;
    private int mSlotId;
    private int mState = 1;

    private BipIconLoader(Looper looper, IccFileHandler fh, int slotId) {
        super(looper);
        this.mSimFH = fh;
        this.mSlotId = slotId;
        this.mIconsCache = new HashMap<>(50);
    }

    static BipIconLoader getInstance(Handler caller, IccFileHandler fh, int slotId) {
        BipIconLoader[] bipIconLoaderArr = sLoader;
        if (!(bipIconLoaderArr == null || bipIconLoaderArr[slotId] == null)) {
            return bipIconLoaderArr[slotId];
        }
        if (sThread == null) {
            sSimCount = TelephonyManager.getDefault().getSimCount();
            sThread = new HandlerThread[sSimCount];
            for (int i = 0; i < sSimCount; i++) {
                sThread[i] = null;
            }
        }
        if (sLoader == null) {
            sSimCount = TelephonyManager.getDefault().getSimCount();
            sLoader = new BipIconLoader[sSimCount];
            for (int i2 = 0; i2 < sSimCount; i2++) {
                sLoader[i2] = null;
            }
        }
        if (fh != null) {
            HandlerThread[] handlerThreadArr = sThread;
            if (handlerThreadArr[slotId] == null) {
                handlerThreadArr[slotId] = new HandlerThread("BIP Icon Loader");
                sThread[slotId].start();
            }
            if (sLoader[slotId] != null || sThread[slotId].getLooper() == null) {
                return null;
            }
            sLoader[slotId] = new BipIconLoader(sThread[slotId].getLooper(), fh, slotId);
            return sLoader[slotId];
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void loadIcons(int[] recordNumbers, Message msg) {
        if (recordNumbers != null && recordNumbers.length != 0 && msg != null) {
            this.mEndMsg = msg;
            this.mIcons = new Bitmap[recordNumbers.length];
            this.mRecordNumbers = recordNumbers;
            this.mCurrentRecordIndex = 0;
            this.mState = 2;
            startLoadingIcon(recordNumbers[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public void loadIcon(int recordNumber, Message msg) {
        if (msg != null) {
            this.mEndMsg = msg;
            this.mState = 1;
            startLoadingIcon(recordNumber);
        }
    }

    private void startLoadingIcon(int recordNumber) {
        MtkCatLog.d(TAG, "call startLoadingIcon");
        this.mId = null;
        this.mIconData = null;
        this.mCurrentIcon = null;
        this.mRecordNumber = recordNumber;
        if (this.mIconsCache.containsKey(Integer.valueOf(recordNumber))) {
            MtkCatLog.d(TAG, "mIconsCache contains record " + recordNumber);
            this.mCurrentIcon = this.mIconsCache.get(Integer.valueOf(recordNumber));
            postIcon();
            return;
        }
        MtkCatLog.d(TAG, "to load icon from EFimg");
        readId();
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
                byte[] rawData = (byte[]) ar.result;
                MtkCatLog.d(TAG, "EFimg raw data: " + HexDump.toHexString(rawData));
                if (handleImageDescriptor((byte[]) ar.result)) {
                    readIconData();
                    return;
                }
                throw new Exception("Unable to parse image descriptor");
            } else if (i == 2) {
                MtkCatLog.d(TAG, "load icon done");
                byte[] rawData2 = (byte[]) ((AsyncResult) msg.obj).result;
                MtkCatLog.d(TAG, "icon raw data: " + HexDump.toHexString(rawData2));
                MtkCatLog.d(TAG, "load icon CODING_SCHEME = " + this.mId.mCodingScheme);
                if (this.mId.mCodingScheme == 17) {
                    this.mCurrentIcon = parseToBnW(rawData2, rawData2.length);
                    this.mIconsCache.put(Integer.valueOf(this.mRecordNumber), this.mCurrentIcon);
                    postIcon();
                } else if (this.mId.mCodingScheme == 33) {
                    this.mIconData = rawData2;
                    readClut();
                } else {
                    MtkCatLog.d(TAG, "else  /postIcon ");
                    postIcon();
                }
            } else if (i == 3) {
                MtkCatLog.d(TAG, "load clut done");
                this.mCurrentIcon = parseToRGB(this.mIconData, this.mIconData.length, false, (byte[]) ((AsyncResult) msg.obj).result);
                this.mIconsCache.put(Integer.valueOf(this.mRecordNumber), this.mCurrentIcon);
                postIcon();
            }
        } catch (Exception e) {
            MtkCatLog.d(this, "Icon load failed");
            e.printStackTrace();
            postIcon();
        }
    }

    private boolean handleImageDescriptor(byte[] rawData) {
        MtkCatLog.d(TAG, "call handleImageDescriptor");
        this.mId = ImageDescriptor.parse(rawData, 1);
        if (this.mId == null) {
            MtkCatLog.d(TAG, "fail to parse image raw data");
            return false;
        }
        MtkCatLog.d(TAG, "success to parse image raw data");
        return true;
    }

    private void readClut() {
        Message msg = obtainMessage(3);
        IccFileHandler iccFileHandler = this.mSimFH;
        int i = this.mId.mImageId;
        byte[] bArr = this.mIconData;
        iccFileHandler.loadEFImgTransparent(i, bArr[4], bArr[5], this.mIconData[3] * 3, msg);
    }

    private void readId() {
        MtkCatLog.d(TAG, "call readId");
        if (this.mRecordNumber < 0) {
            this.mCurrentIcon = null;
            postIcon();
            return;
        }
        this.mSimFH.loadEFImgLinearFixed(this.mRecordNumber, obtainMessage(1));
    }

    private void readIconData() {
        MtkCatLog.d(TAG, "call readIconData");
        this.mSimFH.loadEFImgTransparent(this.mId.mImageId, 0, 0, this.mId.mLength, obtainMessage(2));
    }

    private void postIcon() {
        int i = this.mState;
        if (i == 1) {
            Message message = this.mEndMsg;
            message.obj = this.mCurrentIcon;
            message.sendToTarget();
        } else if (i == 2) {
            Bitmap[] bitmapArr = this.mIcons;
            int i2 = this.mCurrentRecordIndex;
            this.mCurrentRecordIndex = i2 + 1;
            bitmapArr[i2] = this.mCurrentIcon;
            int i3 = this.mCurrentRecordIndex;
            int[] iArr = this.mRecordNumbers;
            if (i3 < iArr.length) {
                startLoadingIcon(iArr[i3]);
                return;
            }
            Message message2 = this.mEndMsg;
            message2.obj = bitmapArr;
            message2.sendToTarget();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r8v4 int: [D('pixelIndex' int), D('valueIndex' int)] */
    /* JADX INFO: Multiple debug info for r2v3 byte: [D('currentByte' byte), D('valueIndex' int)] */
    public static Bitmap parseToBnW(byte[] data, int length) {
        int valueIndex = 0 + 1;
        int width = data[0] & PplMessageManager.Type.INVALID;
        int valueIndex2 = valueIndex + 1;
        int height = data[valueIndex] & PplMessageManager.Type.INVALID;
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        int pixelIndex = 0;
        byte bitIndex = 7;
        byte currentByte = 0;
        while (pixelIndex < numOfPixels) {
            if (pixelIndex % 8 == 0) {
                bitIndex = 7;
                currentByte = data[valueIndex2];
                valueIndex2++;
            }
            pixels[pixelIndex] = bitToBnW((currentByte >> bitIndex) & 1);
            pixelIndex++;
            bitIndex--;
        }
        if (pixelIndex != numOfPixels) {
            MtkCatLog.d("BipIconLoader", "parseToBnW; size error");
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    private static int bitToBnW(int bit) {
        if (bit == 1) {
            return -1;
        }
        return -16777216;
    }

    public static Bitmap parseToRGB(byte[] data, int length, boolean transparency, byte[] clut) {
        int valueIndex = 0 + 1;
        int width = data[0] & PplMessageManager.Type.INVALID;
        int valueIndex2 = valueIndex + 1;
        int height = data[valueIndex] & PplMessageManager.Type.INVALID;
        int valueIndex3 = valueIndex2 + 1;
        int bitsPerImg = data[valueIndex2] & PplMessageManager.Type.INVALID;
        int i = valueIndex3 + 1;
        int numOfClutEntries = data[valueIndex3] & PplMessageManager.Type.INVALID;
        boolean bitsOverlaps = false;
        if (true == transparency) {
            clut[numOfClutEntries - 1] = 0;
        }
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        int pixelIndex = 0;
        int bitsStartOffset = 8 - bitsPerImg;
        int bitIndex = bitsStartOffset;
        int valueIndex4 = 6 + 1;
        byte currentByte = data[6];
        int mask = getMask(bitsPerImg);
        if (8 % bitsPerImg == 0) {
            bitsOverlaps = true;
        }
        while (pixelIndex < numOfPixels) {
            if (bitIndex < 0) {
                int valueIndex5 = valueIndex4 + 1;
                currentByte = data[valueIndex4];
                bitIndex = bitsOverlaps ? bitsStartOffset : bitIndex * -1;
                valueIndex4 = valueIndex5;
            }
            int clutIndex = ((currentByte >> bitIndex) & mask) * 3;
            pixels[pixelIndex] = Color.rgb((int) clut[clutIndex], (int) clut[clutIndex + 1], (int) clut[clutIndex + 2]);
            bitIndex -= bitsPerImg;
            pixelIndex++;
            numOfClutEntries = numOfClutEntries;
            currentByte = currentByte;
            bitsOverlaps = bitsOverlaps;
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    private static int getMask(int numOfBits) {
        switch (numOfBits) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 3:
                return 7;
            case 4:
                return 15;
            case 5:
                return 31;
            case 6:
                return 63;
            case 7:
                return 127;
            case 8:
                return 255;
            default:
                return 0;
        }
    }

    public void dispose() {
        this.mSimFH = null;
        HandlerThread[] handlerThreadArr = sThread;
        if (handlerThreadArr != null) {
            int i = this.mSlotId;
            if (handlerThreadArr[i] != null) {
                handlerThreadArr[i].quit();
                sThread[this.mSlotId] = null;
            }
        }
        BipIconLoader[] bipIconLoaderArr = sLoader;
        if (bipIconLoaderArr != null) {
            int i2 = this.mSlotId;
            if (bipIconLoaderArr[i2] != null) {
                bipIconLoaderArr[i2] = null;
            }
        }
        int i3 = 0;
        while (i3 < sSimCount && sThread[i3] == null) {
            i3++;
        }
        if (i3 == sSimCount) {
            sThread = null;
            sLoader = null;
        }
        this.mIconsCache = null;
    }
}
