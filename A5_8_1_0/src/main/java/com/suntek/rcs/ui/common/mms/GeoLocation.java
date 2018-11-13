package com.suntek.rcs.ui.common.mms;

public class GeoLocation {
    private String lable;
    private double lat;
    private double lng;

    public double getLat() {
        return this.lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return this.lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getLabel() {
        return this.lable;
    }

    public void setLabel(String lable) {
        this.lable = lable;
    }

    public String toString() {
        return "GeoLocation [lat=" + this.lat + ", lng=" + this.lng + ", lable=" + this.lable + "]";
    }
}
