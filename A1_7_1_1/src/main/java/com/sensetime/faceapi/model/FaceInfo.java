package com.sensetime.faceapi.model;

import android.graphics.PointF;
import android.graphics.Rect;

public class FaceInfo {
    public float eyeDist;
    public PointF[] facePoints;
    public Rect faceRect;
    public int id;
    public float pitch;
    public float roll;
    public float score;
    public float yaw;

    public FaceInfo clone() {
        FaceInfo faceInfo = new FaceInfo();
        faceInfo.faceRect = new Rect();
        faceInfo.facePoints = new PointF[this.facePoints.length];
        faceInfo.faceRect.set(this.faceRect);
        for (int i = 0; i < faceInfo.facePoints.length; i++) {
            faceInfo.facePoints[i] = new PointF();
            faceInfo.facePoints[i].set(this.facePoints[i]);
        }
        faceInfo.id = this.id;
        faceInfo.score = this.score;
        faceInfo.yaw = this.yaw;
        faceInfo.pitch = this.pitch;
        faceInfo.roll = this.roll;
        faceInfo.eyeDist = this.eyeDist;
        return faceInfo;
    }

    public String toString() {
        return "rect: " + this.faceRect.toShortString() + ", yaw: " + this.yaw + ", pitch: " + this.pitch + ", roll: " + this.roll;
    }
}
