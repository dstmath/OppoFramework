package com.mediatek.internal.telephony.gsm.cbutil;

public class Circle extends Shape {
    public double mRadius;
    public Vertex mVertex;

    public Circle() {
        this.mType = 1;
    }

    public Circle(Vertex vertex, double radius) {
        this.mType = 1;
        this.mVertex = vertex;
        this.mRadius = radius;
    }

    public String toString() {
        return "Circle {" + this.mVertex.toString() + ", Radius = " + this.mRadius + '}';
    }
}
