package com.mediatek.internal.telephony.gsm.cbutil;

public class Vertex {
    public double mLati;
    public double mLongi;

    public Vertex(double x, double y) {
        this.mLati = x;
        this.mLongi = y;
    }

    public String toString() {
        return "Vertex {lati = " + this.mLati + ", longi = " + this.mLongi + '}';
    }
}
