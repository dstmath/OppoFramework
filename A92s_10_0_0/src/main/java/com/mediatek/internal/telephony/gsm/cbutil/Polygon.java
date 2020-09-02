package com.mediatek.internal.telephony.gsm.cbutil;

import java.util.ArrayList;
import java.util.Iterator;

public class Polygon extends Shape {
    public ArrayList<Vertex> mVertices;

    public Polygon() {
        this.mType = 2;
        this.mVertices = new ArrayList<>();
    }

    public Polygon(ArrayList<Vertex> vertices) {
        this.mType = 2;
        this.mVertices = vertices;
    }

    public void addVertex(Vertex vertex) {
        this.mVertices.add(vertex);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Polygon {" + this.mVertices.size() + ",");
        Iterator<Vertex> it = this.mVertices.iterator();
        while (it.hasNext()) {
            sb.append(it.next().toString() + ",");
        }
        sb.append("}");
        return sb.toString();
    }
}
