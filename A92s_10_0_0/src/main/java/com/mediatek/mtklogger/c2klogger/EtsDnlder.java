package com.mediatek.mtklogger.c2klogger;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;

public class EtsDnlder extends EtsDevice {
    public static String[] sSectionName = {"BOOT", "CP"};
    private EtsDnlderCallback mCallback = null;
    private EtsDnlderThread mDnlderThr;
    private List<String> mImgFiles;
    private int mSizeOnePackage = 260;

    public enum CBPMode {
        Boot,
        CP,
        Unknown
    }

    public enum DnldStatus {
        Readying,
        WaitingBoot,
        Erasing,
        Downloading,
        Finishied,
        Error
    }

    public interface EtsDnlderCallback {
        void onProcess(DnldStatus dnldStatus, int i, String str);
    }

    public EtsDnlderCallback getCallback() {
        return this.mCallback;
    }

    public EtsDnlder(int sizeOnePackage, EtsDnlderCallback callback) {
        this.mSizeOnePackage = sizeOnePackage;
        if (callback != null) {
            this.mCallback = callback;
        } else {
            this.mCallback = new EtsDnlderCallback() {
                /* class com.mediatek.mtklogger.c2klogger.EtsDnlder.AnonymousClass1 */

                @Override // com.mediatek.mtklogger.c2klogger.EtsDnlder.EtsDnlderCallback
                public void onProcess(DnldStatus status, int progress, String info) {
                    if (status == DnldStatus.Error) {
                        Log.e("via_ets", info);
                    } else if (status == DnldStatus.Downloading) {
                        Log.i("via_ets", info + " progress:" + progress);
                    } else {
                        Log.i("via_ets", info);
                    }
                }
            };
        }
    }

    public List<String> getImgFiles() {
        return this.mImgFiles;
    }

    public byte getFlashSectionIndex(String imgFile) {
        String pathnameFile = imgFile.toLowerCase();
        if (pathnameFile.indexOf("boot") > 0) {
            return 0;
        }
        if (pathnameFile.indexOf("cp") > 0) {
            return 1;
        }
        Log.e("via_ets", "unknown section for image:\"" + imgFile + "\"");
        return -1;
    }

    public void close() {
        destroy();
    }

    public boolean test() {
        if (!create("/dev/ttyUSB1").booleanValue()) {
            Log.e("test", "open the ets device failed");
            return false;
        }
        try {
            Thread.sleep(3000);
            Log.i("test", "do loopback");
            int retry = 3;
            while (retry > 0 && !loopback()) {
                retry--;
            }
            if (retry == 0) {
                close();
                return false;
            }
            Log.i("test", "do jump to boot mode");
            if (!jump2load(CBPMode.Boot, true)) {
                close();
                return false;
            }
            Log.i("test", "do erase the cp section");
            if (!eraseFlash((byte) 1)) {
                close();
                return false;
            }
            Log.i("test", "test end with successful");
            close();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean testJump2Boot() {
        if (!create("/dev/ttyUSB1").booleanValue()) {
            Log.e("test", "open the ets device failed");
            return false;
        }
        try {
            Thread.sleep(3000);
            Log.i("test", "do loopback");
            int retry = 3;
            while (retry > 0 && !loopback()) {
                retry--;
            }
            if (retry == 0) {
                close();
                return false;
            }
            Log.i("test", "do jump to boot mode");
            if (!jump2load(CBPMode.Boot, true)) {
                close();
                return false;
            }
            Log.i("test", "test end with successful");
            close();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void start(String pathDev, List<String> pathsImg) {
        this.mCallback.onProcess(DnldStatus.Readying, 0, "Checking the image file");
        if (pathsImg == null || pathsImg.isEmpty()) {
            Log.e("via_ets", "no image filenames");
        }
        this.mImgFiles = pathsImg;
        this.mCallback.onProcess(DnldStatus.Readying, 0, "Openning the ets device");
        if (!create(pathDev).booleanValue()) {
            Log.e("via_ets", "Open the ets device failed");
        }
        this.mDnlderThr = new EtsDnlderThread(this);
        this.mDnlderThr.start();
    }

    public CBPMode checkMode() {
        Log.v("via_ets", "do check mode");
        EtsMsg msg = sendAndWait(new EtsMsg(200, null), 200, 1000);
        CBPMode ret = CBPMode.CP;
        if (msg == null) {
            ret = CBPMode.Boot;
        }
        Log.v("via_ets", ret.name() + " mode");
        return ret;
    }

    public boolean jump2load(CBPMode toMode, boolean openAgain) {
        Log.v("via_ets", "do jump to loader to " + toMode.name());
        CBPMode curMode = checkMode();
        if (curMode == CBPMode.Unknown) {
            Log.e("via_ets", "no response from device?");
            return false;
        } else if (curMode == toMode) {
            return true;
        } else {
            write(new EtsMsg(220, null));
            destroy();
            if (openAgain) {
                closeDevice();
                Log.v("via_ets", "wait to open device in " + toMode + " mode");
                try {
                    Thread.sleep(toMode == CBPMode.Boot ? 8000 : 6000);
                    try {
                        openDevice(null);
                        if (toMode == CBPMode.Boot && waitForMsg(224, 5000) == null) {
                            Log.e("via_ets", "check boot2load msg failed, try to check the mode");
                            if (checkMode() == CBPMode.Boot) {
                                return true;
                            }
                            Log.e("via_ets", "the cbp is not in boot mode");
                            return false;
                        }
                    } catch (SecurityException e) {
                        Log.e("via_ets", "create port failed");
                        e.printStackTrace();
                        return false;
                    } catch (InvalidParameterException e2) {
                        Log.e("via_ets", "create port failed");
                        e2.printStackTrace();
                        return false;
                    } catch (IOException e3) {
                        Log.e("via_ets", "create port failed");
                        e3.printStackTrace();
                        return false;
                    }
                } catch (InterruptedException e4) {
                    e4.printStackTrace();
                    return false;
                }
            }
            return true;
        }
    }

    public boolean eraseFlash(byte flashSection) {
        if (sendAndWait(new EtsMsg(1201, new byte[]{flashSection}), 1201, flashSection == 0 ? 30000 : 500000) == null) {
            Log.e("via_ets", "erase flash time out");
            return false;
        }
        Log.i("via_ets", "erase flash success");
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b8, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b9, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0193, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0194, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x019d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01a5, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01a6, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x002d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:4:0x0020, B:27:0x00b3] */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0182 A[LOOP:2: B:36:0x00ee->B:66:0x0182, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x019d A[ExcHandler: IOException (e java.io.IOException), Splitter:B:1:0x0006] */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x002d A[ExcHandler: IOException (e java.io.IOException), Splitter:B:4:0x0020] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x017e A[SYNTHETIC] */
    public boolean downloadFlash(byte flashSection, String imgPath) {
        boolean z;
        byte[] dataImg;
        byte[] block;
        FileInputStream fileImg;
        int seqAck;
        EtsDnlder etsDnlder = this;
        boolean ret = true;
        try {
            File f = new File(imgPath);
            byte[] dataImg2 = new byte[((int) f.length())];
            FileInputStream fileImg2 = new FileInputStream(f);
            int sizeImg = fileImg2.read(dataImg2);
            if (sizeImg != dataImg2.length) {
                try {
                    etsDnlder.mCallback.onProcess(DnldStatus.Error, 0, "Read image file failed");
                    fileImg2.close();
                    return false;
                } catch (FileNotFoundException e) {
                    e = e;
                    z = false;
                    e.printStackTrace();
                    return z;
                } catch (IOException e2) {
                }
            } else {
                fileImg2.close();
                etsDnlder.mCallback.onProcess(DnldStatus.Readying, 0, "Computing the checksum of image");
                byte[] imgLength = EtsUtil.int2bytes(sizeImg);
                byte[] imgChecksum = EtsUtil.int2bytes(EtsUtil.checkSum2(dataImg2));
                byte[] imgInfo = new byte[8];
                int length = imgLength.length;
                int index = 0;
                int index2 = 0;
                while (index2 < length) {
                    imgInfo[index] = imgLength[index2];
                    index2++;
                    index++;
                }
                int length2 = imgChecksum.length;
                int i = 0;
                while (i < length2) {
                    imgInfo[index] = imgChecksum[i];
                    i++;
                    index++;
                }
                etsDnlder.mCallback.onProcess(DnldStatus.Erasing, 0, "Erasing flash, section=" + sSectionName[flashSection]);
                int seqToWrite = 0 + 1;
                if (etsDnlder.sendAndWait(new EtsMsgProgCmd(seqToWrite, EtsMsgProgCmd.sEarse, flashSection, imgInfo), EtsMsgProgCmd.sID, flashSection == 0 ? 30000 : 500000) == null) {
                    Log.e("via_ets", "erase flash time out");
                    return false;
                }
                Log.i("via_ets", "erase flash success");
                etsDnlder.mCallback.onProcess(DnldStatus.Downloading, 0, "Downloading flash, section=" + sSectionName[flashSection]);
                byte[] block2 = new byte[etsDnlder.mSizeOnePackage];
                int numBlocks = (sizeImg / etsDnlder.mSizeOnePackage) + 2;
                int seqAck2 = 0;
                int index3 = 0;
                while (true) {
                    try {
                        int size = sizeImg - index3 > etsDnlder.mSizeOnePackage ? etsDnlder.mSizeOnePackage : sizeImg - index3;
                        if (size > 0) {
                            byte[] data = block2;
                            block = block2;
                            if (size < etsDnlder.mSizeOnePackage) {
                                data = new byte[size];
                            }
                            int i2 = 0;
                            while (i2 < size) {
                                data[i2] = dataImg2[index3];
                                i2++;
                                index3++;
                            }
                            seqToWrite++;
                            dataImg = dataImg2;
                            etsDnlder.write(new EtsMsgProgCmd(seqToWrite, EtsMsgProgCmd.sWaite, flashSection, data));
                        } else {
                            block = block2;
                            dataImg = dataImg2;
                        }
                        if (seqToWrite - seqAck2 <= 3) {
                            if (size > 0) {
                                fileImg = fileImg2;
                                seqAck = seqAck2;
                                etsDnlder.mCallback.onProcess(DnldStatus.Downloading, (seqAck * 100) / numBlocks, "Downloading flash " + sSectionName[flashSection]);
                                if (seqToWrite > seqAck) {
                                    return ret;
                                }
                                etsDnlder = this;
                                seqAck2 = seqAck;
                                fileImg2 = fileImg;
                                ret = ret;
                                block2 = block;
                                dataImg2 = dataImg;
                            }
                        }
                        fileImg = fileImg2;
                        EtsMsg msg = etsDnlder.waitForMsg(EtsMsgProgCmd.sID, 2000);
                        if (msg == null) {
                            Log.e("via_ets", "don't get response");
                            return false;
                        }
                        seqAck = msg.getProgRspSequence();
                        if (msg.getProgRspAck() == 1) {
                            Log.e("via_ets", "get NAK!");
                            return false;
                        }
                        etsDnlder.mCallback.onProcess(DnldStatus.Downloading, (seqAck * 100) / numBlocks, "Downloading flash " + sSectionName[flashSection]);
                        if (seqToWrite > seqAck) {
                        }
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        z = false;
                        e.printStackTrace();
                        return z;
                    } catch (IOException e4) {
                        e = e4;
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            z = false;
            e.printStackTrace();
            return z;
        } catch (IOException e6) {
        }
    }
}
