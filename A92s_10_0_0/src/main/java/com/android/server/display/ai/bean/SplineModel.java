package com.android.server.display.ai.bean;

public class SplineModel implements Cloneable {
    private float[] xs;
    private float[] ys;

    public float[] getXs() {
        return this.xs;
    }

    public void setXs(float[] xs2) {
        this.xs = xs2;
    }

    public float[] getYs() {
        return this.ys;
    }

    public void setYs(float[] ys2) {
        this.ys = ys2;
    }

    public String toString() {
        if (this.xs == null || this.ys == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("x = ");
        for (float x : this.xs) {
            stringBuilder.append(x + ", ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length() - 1);
        stringBuilder.append("y = ");
        for (float y : this.ys) {
            stringBuilder.append(y + ", ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    @Override // java.lang.Object
    public SplineModel clone() throws CloneNotSupportedException {
        SplineModel splineModel = (SplineModel) super.clone();
        float[] fArr = this.xs;
        if (!(fArr == null || this.ys == null)) {
            splineModel.setXs((float[]) fArr.clone());
            splineModel.setYs((float[]) this.ys.clone());
        }
        return splineModel;
    }
}
