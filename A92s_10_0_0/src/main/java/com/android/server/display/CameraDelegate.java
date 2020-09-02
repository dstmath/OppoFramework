package com.android.server.display;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.util.MathUtils;
import android.util.Slog;
import com.android.server.am.ColorGameSpaceManager;
import com.android.server.storage.ColorStorageUtils;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import display.DevicePropertyHelper;
import java.io.File;
import java.io.FileWriter;

public class CameraDelegate {
    private static final int ADJUSTMENT_GALLERY_IN = 16385;
    private static final int ADJUSTMENT_GALLERY_OUT = 32769;
    private static final int ADJUSTMENT_INVERSE_OFF = 32768;
    private static final int ADJUSTMENT_INVERSE_ON = 16384;
    private static final int ADJUSTMENT_VIDEO_IN = 16386;
    private static final int ADJUSTMENT_VIDEO_OUT = 32770;
    private static final int LBR_MODE_LEVEL_MAX = 63;
    private static final int SEED_LBR_LOW_LUX = 15000;
    private static final String SEED_LBR_NODE = "/sys/kernel/oppo_display/seed";
    private static final int SEED_LBR_STEP_SIZE = 235;
    private static boolean mCameraBacklight = false;
    private static int mCameraMode = -1;
    private static boolean mCameraUseAdjustmentSetting = false;
    private static boolean mGalleryBacklight = false;
    private static boolean mSeedLbrModeSupport = false;
    private int[] cameraGallerySamsung = {0, 1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 32, 36, 40, 44, 47, 50, 53, 56, 59, 62, 65, 68, 71, 74, 77, 80, 83, 85, 87, 89, 91, 94, 97, 100, ColorStorageUtils.DATA_SIMU_MODE_FULL, ColorStorageUtils.DATA_SIMU_MODE_REAL_FULL, 106, 108, 110, 112, 114, 116, 118, ColorGameSpaceManager.MSG_SCREEN_ON, 122, 124, 126, 128, ColorGameSpaceManager.MSG_DEVICE_UPDATE, 132, 134, 136, 138, ColorGameSpaceManager.MSG_SEND_GAME_ENTER, 142, 144, 146, 147, 148, 149, ColorStartingWindowContants.DEFAULT_STARTING_WINDOW_EXIT_ANIMATION_DURATION, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 199, 200, 201, 201, 202, 203, 203, 204, 205, 205, 206, 207, 207, 208, 209, 209, 210, 210, 211, 211, 212, 212, 213, 213, 214, 214, 215, 215, 216, 216, 217, 217, 218, 218, 219, 219, 220, 220, 220, 221, 221, 222, 222, 222, 223, 223, 224, 224, 224, 225, 225, 226, 226, 226, 227, 227, 228, 228, 228, 229, 229, 230, 230, 230, 231, 231, 232, 232, 232, 233, 233, 234, 234, 234, SEED_LBR_STEP_SIZE, SEED_LBR_STEP_SIZE, 236, 236, 236, 237, 237, 238, 238, 238, 239, 239, 240, 240, 240, 241, 241, 241, 242, 242, 242, 243, 243, 243, 244, 244, 244, 245, 245, 245, 246, 246, 246, 247, 247, 247, 247, 248, 248, 248, 249, 249, 249, 249, 250, 250, 250, 250, 251, 251, 251, 251, 252, 252, 252, 252, 253, 253, 253, 253, 254, 254, 254, 254, 254, 255, 255, 255, 255, 255, 255};

    CameraDelegate() {
    }

    /* access modifiers changed from: package-private */
    public void readLbrSwitch(Context context) {
        mSeedLbrModeSupport = context.getPackageManager().hasSystemFeature("oppo.seed.lbr.support");
    }

    /* access modifiers changed from: package-private */
    public int adjustCameraBright(int screenAutoBrightness) {
        if (mCameraMode != 1) {
            return screenAutoBrightness;
        }
        if (mCameraBacklight) {
            int screenAutoBrightness2 = adjustCameraBrightness(screenAutoBrightness, OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getMaximumScreenBrightnessSetting());
            Slog.d("ColorAutomaticBrightnessController", "updateBrightness from CAMERA color = " + screenAutoBrightness2);
            return screenAutoBrightness2;
        } else if (!mGalleryBacklight) {
            return screenAutoBrightness;
        } else {
            Slog.d("ColorAutomaticBrightnessController", "There is no request in Gallery, do nothing");
            return screenAutoBrightness;
        }
    }

    /* access modifiers changed from: package-private */
    public int getCameraMode() {
        return mCameraMode;
    }

    /* access modifiers changed from: package-private */
    public void setCameraMode(int mode) {
        mCameraMode = mode;
    }

    /* access modifiers changed from: package-private */
    public void setCameraUseAdjustmentSetting(boolean enable) {
        mCameraUseAdjustmentSetting = enable;
    }

    public boolean getCameraUseAdjustmentSetting() {
        return mCameraUseAdjustmentSetting;
    }

    /* access modifiers changed from: package-private */
    public void setCameraBacklight(boolean enable) {
        mCameraBacklight = enable;
    }

    /* access modifiers changed from: package-private */
    public void setGalleryBacklight(boolean enable) {
        mGalleryBacklight = enable;
    }

    public void writeSeedLbrNodeValue(int value) {
        writeFileNodeValue(SEED_LBR_NODE, value);
    }

    public void writeFileNodeValue(String str, int value) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(str));
            writer.write(String.valueOf(value));
            try {
                writer.close();
            } catch (Exception e) {
                Slog.d("ColorAutomaticBrightnessController", "writeFileNodeValue io stream close wrong");
            }
        } catch (Exception e2) {
            Slog.d("ColorAutomaticBrightnessController", "writeFileNodeValue sorry write wrong");
            e2.printStackTrace();
            if (writer != null) {
                writer.close();
            }
        } catch (Throwable th) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e3) {
                    Slog.d("ColorAutomaticBrightnessController", "writeFileNodeValue io stream close wrong");
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSpecialAdj(float value) {
        boolean specialAdj = false;
        int mMaxBrightness = OppoFeatureCache.getOrCreate(IOppoBrightness.DEFAULT, new Object[0]).getMaximumScreenBrightnessSetting();
        if (value == (((float) mMaxBrightness) * 500.0f) / 255.0f || value == (((float) mMaxBrightness) * 300.0f) / 255.0f || value == (((float) mMaxBrightness) * 301.0f) / 255.0f || value == 16384.0f || value == 32768.0f || value == 16385.0f || value == 32769.0f || value == 16386.0f || Math.round(value) == 1930286 || Math.round(value) == 1930287 || value == 32770.0f) {
            specialAdj = true;
        }
        Slog.d("ColorAutomaticBrightnessController", "isSpecialAdj=" + specialAdj + " value=" + value);
        return specialAdj;
    }

    private int adjustCameraBrightness(int brightness, int normalMaxBrightness) {
        if (1 != DevicePropertyHelper.sBrightnessBitsConfig) {
            float uniform_brightness = ((float) brightness) / ((float) normalMaxBrightness);
            float adjGamma = 0.6f;
            if (uniform_brightness > 0.196f) {
                adjGamma = 0.4f;
            }
            int adjust_brightness = Math.round(((float) normalMaxBrightness) * MathUtils.pow(uniform_brightness, adjGamma));
            Slog.d("ColorAutomaticBrightnessController", "adjust_brightness = " + adjust_brightness);
            return adjust_brightness;
        } else if (brightness >= 256 || brightness <= 0) {
            return 128;
        } else {
            return this.cameraGallerySamsung[brightness];
        }
    }
}
