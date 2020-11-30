package com.color.zoomwindow;

import android.graphics.Rect;
import android.graphics.Region;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class ColorZoomWindowRegion implements Parcelable {
    public static final Parcelable.Creator<ColorZoomWindowRegion> CREATOR = new Parcelable.Creator<ColorZoomWindowRegion>() {
        /* class com.color.zoomwindow.ColorZoomWindowRegion.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorZoomWindowRegion createFromParcel(Parcel in) {
            return new ColorZoomWindowRegion(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorZoomWindowRegion[] newArray(int size) {
            return new ColorZoomWindowRegion[size];
        }
    };
    private List<Rect> mRectList = new ArrayList();

    public Region getRegion() {
        Region region = new Region();
        for (int i = 0; i < this.mRectList.size(); i++) {
            region.op(this.mRectList.get(i), Region.Op.UNION);
        }
        return region;
    }

    public List<Rect> getRectList() {
        return this.mRectList;
    }

    public ColorZoomWindowRegion() {
    }

    public ColorZoomWindowRegion(Parcel in) {
        this.mRectList = in.createTypedArrayList(Rect.CREATOR);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mRectList);
    }

    public String toString() {
        return getRegion().toString();
    }
}
