package androidx.viewpager.widget;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

public abstract class PagerAdapter {
    public abstract int getCount();

    public abstract boolean isViewFromObject(View view, Object obj);

    public void startUpdate(ViewGroup container) {
        startUpdate((View) container);
    }

    public Object instantiateItem(ViewGroup container, int position) {
        return instantiateItem((View) container, position);
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        destroyItem((View) container, position, object);
    }

    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        setPrimaryItem((View) container, position, object);
    }

    public void finishUpdate(ViewGroup container) {
        finishUpdate((View) container);
    }

    @Deprecated
    public void startUpdate(View container) {
    }

    @Deprecated
    public Object instantiateItem(View container, int position) {
        throw new UnsupportedOperationException("Required method instantiateItem was not overridden");
    }

    @Deprecated
    public void destroyItem(View container, int position, Object object) {
        throw new UnsupportedOperationException("Required method destroyItem was not overridden");
    }

    @Deprecated
    public void setPrimaryItem(View container, int position, Object object) {
    }

    @Deprecated
    public void finishUpdate(View container) {
    }

    public Parcelable saveState() {
        return null;
    }

    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    public float getPageWidth(int position) {
        return 1.0f;
    }
}
