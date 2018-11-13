package com.qualcomm.qti.camera;

import android.hardware.Camera.Face;
import android.os.Bundle;

public class ExtendedFace extends Face {
    private static final String BUNDLE_KEY_BLINK_DETECTED = "blinkDetected";
    private static final String BUNDLE_KEY_FACE_PITCH_DEGREE = "facePitchDegree";
    private static final String BUNDLE_KEY_FACE_RECOGNIZED = "faceRecognized";
    private static final String BUNDLE_KEY_FACE_ROLL_DEGREE = "faceRollDegree";
    private static final String BUNDLE_KEY_FACE_YAW_DEGREE = "faceYawDegree";
    private static final String BUNDLE_KEY_GAZE_LEFT_RIGHT_DEGREE = "gazeLeftRightDegree";
    private static final String BUNDLE_KEY_GAZE_UP_DOWN_DEGREE = "gazeUpDownDegree";
    private static final String BUNDLE_KEY_LEFT_EYE_CLOSED_VALUE = "leftEyeClosedValue";
    private static final String BUNDLE_KEY_RIGHT_EYE_CLOSED_VALUE = "rightEyeClosedValue";
    private static final String BUNDLE_KEY_SMILE_SCORE = "smileScore";
    private static final String BUNDLE_KEY_SMILE_VALUE = "smileValue";
    private static final String STR_FALSE = "false";
    private static final String STR_TRUE = "true";
    private int blinkDetected = 0;
    private int faceRecognized = 0;
    private int gazeAngle = 0;
    private int leftrightDir = 0;
    private int leftrightGaze = 0;
    private int leyeBlink = 0;
    private int reyeBlink = 0;
    private int rollDir = 0;
    private int smileDegree = 0;
    private int smileScore = 0;
    private int topbottomGaze = 0;
    private int updownDir = 0;

    public int getSmileDegree() {
        return this.smileDegree;
    }

    public int getSmileScore() {
        return this.smileScore;
    }

    public int getBlinkDetected() {
        return this.blinkDetected;
    }

    public int getFaceRecognized() {
        return this.faceRecognized;
    }

    public int getGazeAngle() {
        return this.gazeAngle;
    }

    public int getUpDownDirection() {
        return this.updownDir;
    }

    public int getLeftRightDirection() {
        return this.leftrightDir;
    }

    public int getRollDirection() {
        return this.rollDir;
    }

    public int getLeftEyeBlinkDegree() {
        return this.leyeBlink;
    }

    public int getRightEyeBlinkDegree() {
        return this.reyeBlink;
    }

    public int getLeftRightGazeDegree() {
        return this.leftrightGaze;
    }

    public int getTopBottomGazeDegree() {
        return this.topbottomGaze;
    }

    public Bundle getExtendedFaceInfo() {
        Bundle faceInfo = new Bundle();
        faceInfo.putInt(BUNDLE_KEY_SMILE_VALUE, this.smileDegree);
        faceInfo.putInt(BUNDLE_KEY_LEFT_EYE_CLOSED_VALUE, this.leyeBlink);
        faceInfo.putInt(BUNDLE_KEY_RIGHT_EYE_CLOSED_VALUE, this.reyeBlink);
        faceInfo.putInt(BUNDLE_KEY_FACE_PITCH_DEGREE, this.updownDir);
        faceInfo.putInt(BUNDLE_KEY_FACE_YAW_DEGREE, this.leftrightDir);
        faceInfo.putInt(BUNDLE_KEY_FACE_ROLL_DEGREE, this.rollDir);
        faceInfo.putInt(BUNDLE_KEY_GAZE_UP_DOWN_DEGREE, this.topbottomGaze);
        faceInfo.putInt(BUNDLE_KEY_GAZE_LEFT_RIGHT_DEGREE, this.leftrightGaze);
        faceInfo.putInt(BUNDLE_KEY_BLINK_DETECTED, this.blinkDetected);
        faceInfo.putInt(BUNDLE_KEY_SMILE_SCORE, this.smileScore);
        faceInfo.putInt(BUNDLE_KEY_FACE_RECOGNIZED, this.faceRecognized);
        return faceInfo;
    }
}
