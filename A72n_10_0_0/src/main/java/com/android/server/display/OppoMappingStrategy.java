package com.android.server.display;

import android.hardware.display.BrightnessConfiguration;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import com.android.internal.util.Preconditions;
import com.android.server.display.utils.Plog;
import java.io.PrintWriter;

public class OppoMappingStrategy extends BrightnessMappingStrategy {
    private static final Plog PLOG = Plog.createSystemPlog(TAG);
    private static final String TAG = "OppoMappingStrategy";
    private float mAutoBrightnessAdjustment;
    private final float[] mBrightness;
    private final float[] mLux;
    private float mMaxGamma;
    private Spline mSpline;
    private float mUserBrightness;
    private float mUserLux;

    public OppoMappingStrategy() {
        float[] lux = getLuxLevels(mOppoBrightUtils.readAutoBrightnessLuxConfig());
        int[] brightness = mOppoBrightUtils.readAutoBrightnessConfig();
        boolean z = true;
        Preconditions.checkArgument((lux.length == 0 || brightness.length == 0) ? false : true, "Lux and brightness arrays must not be empty!");
        Preconditions.checkArgument(lux.length != brightness.length ? false : z, "Lux and brightness arrays must be the same length!");
        Preconditions.checkArrayElementsInRange(lux, (float) OppoBrightUtils.MIN_LUX_LIMITI, Float.MAX_VALUE, "lux");
        Preconditions.checkArrayElementsInRange(brightness, 0, Integer.MAX_VALUE, "brightness");
        int N = brightness.length;
        this.mLux = new float[N];
        this.mBrightness = new float[N];
        for (int i = 0; i < N; i++) {
            this.mLux[i] = lux[i];
            this.mBrightness[i] = normalizeAbsoluteBrightness(brightness[i]);
        }
        this.mMaxGamma = 1.0f;
        this.mAutoBrightnessAdjustment = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mUserLux = -1.0f;
        this.mUserBrightness = -1.0f;
        if (this.mLoggingEnabled) {
            PLOG.start("oppo mapping strategy");
        }
        computeSpline();
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public boolean setBrightnessConfiguration(BrightnessConfiguration config) {
        return false;
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public float getBrightness(float lux, String packageName, int category) {
        return this.mSpline.interpolate(lux);
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public float getAutoBrightnessAdjustment() {
        return this.mAutoBrightnessAdjustment;
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public boolean setAutoBrightnessAdjustment(float adjustment) {
        float adjustment2 = MathUtils.constrain(adjustment, -1.0f, 1.0f);
        if (adjustment2 == this.mAutoBrightnessAdjustment) {
            return false;
        }
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "setAutoBrightnessAdjustment: " + this.mAutoBrightnessAdjustment + " => " + adjustment2);
            PLOG.start("auto-brightness adjustment");
        }
        this.mAutoBrightnessAdjustment = adjustment2;
        computeSpline();
        return true;
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public float convertToNits(int backlight) {
        return -1.0f;
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public void addUserDataPoint(float lux, float brightness) {
        float unadjustedBrightness = getUnadjustedBrightness(lux);
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "addUserDataPoint: (" + lux + "," + brightness + ")");
            PLOG.start("add user data point").logPoint("user data point", lux, brightness).logPoint("current brightness", lux, unadjustedBrightness);
        }
        float adjustment = inferAutoBrightnessAdjustment(this.mMaxGamma, brightness, unadjustedBrightness);
        if (this.mLoggingEnabled) {
            Slog.d(TAG, "addUserDataPoint: " + this.mAutoBrightnessAdjustment + " => " + adjustment);
        }
        this.mAutoBrightnessAdjustment = adjustment;
        this.mUserLux = lux;
        this.mUserBrightness = brightness;
        computeSpline();
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public void clearUserDataPoints() {
        if (this.mUserLux != -1.0f) {
            if (this.mLoggingEnabled) {
                Slog.d(TAG, "clearUserDataPoints: " + this.mAutoBrightnessAdjustment + " => 0");
                PLOG.start("clear user data points").logPoint("user data point", this.mUserLux, this.mUserBrightness);
            }
            this.mAutoBrightnessAdjustment = OppoBrightUtils.MIN_LUX_LIMITI;
            this.mUserLux = -1.0f;
            this.mUserBrightness = -1.0f;
            computeSpline();
        }
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public boolean hasUserDataPoints() {
        return this.mUserLux != -1.0f;
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public boolean isDefaultConfig() {
        return true;
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public BrightnessConfiguration getDefaultConfig() {
        return null;
    }

    @Override // com.android.server.display.BrightnessMappingStrategy
    public void dump(PrintWriter pw) {
        pw.println(TAG);
        pw.println("  mSpline=" + this.mSpline);
        pw.println("  mMaxGamma=" + this.mMaxGamma);
        pw.println("  mAutoBrightnessAdjustment=" + this.mAutoBrightnessAdjustment);
        pw.println("  mUserLux=" + this.mUserLux);
        pw.println("  mUserBrightness=" + this.mUserBrightness);
    }

    private void computeSpline() {
        getAdjustedCurve(this.mLux, this.mBrightness, this.mUserLux, this.mUserBrightness, this.mAutoBrightnessAdjustment, this.mMaxGamma);
        this.mSpline = Spline.createSpline(this.mLux, this.mBrightness);
    }

    private float getUnadjustedBrightness(float lux) {
        return Spline.createSpline(this.mLux, this.mBrightness).interpolate(lux);
    }

    private static float[] getLuxLevels(int[] lux) {
        float[] levels = new float[(lux.length + 1)];
        for (int i = 0; i < lux.length; i++) {
            levels[i + 1] = (float) lux[i];
        }
        return levels;
    }

    public Spline getOppoMappingStrategySpline() {
        return this.mSpline;
    }
}
