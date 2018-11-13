package android.support.v7.widget;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import java.util.ArrayList;
import java.util.List;

class ChildHelper {
    private static final boolean DEBUG = false;
    private static final String TAG = "ChildrenHelper";
    final Bucket mBucket = new Bucket();
    final Callback mCallback;
    final List<View> mHiddenViews = new ArrayList();

    static class Bucket {
        static final int BITS_PER_WORD = 64;
        static final long LAST_BIT = Long.MIN_VALUE;
        long mData = 0;
        Bucket next;

        Bucket() {
        }

        void set(int index) {
            if (index >= 64) {
                ensureNext();
                this.next.set(index - 64);
                return;
            }
            this.mData |= 1 << index;
        }

        private void ensureNext() {
            if (this.next == null) {
                this.next = new Bucket();
            }
        }

        void clear(int index) {
            if (index < 64) {
                this.mData &= ~(1 << index);
            } else if (this.next != null) {
                this.next.clear(index - 64);
            }
        }

        boolean get(int index) {
            if (index >= 64) {
                ensureNext();
                return this.next.get(index - 64);
            }
            return (this.mData & (1 << index)) != 0 ? true : ChildHelper.DEBUG;
        }

        void reset() {
            this.mData = 0;
            if (this.next != null) {
                this.next.reset();
            }
        }

        void insert(int index, boolean value) {
            if (index >= 64) {
                ensureNext();
                this.next.insert(index - 64, value);
                return;
            }
            boolean lastBit = (this.mData & LAST_BIT) != 0 ? true : ChildHelper.DEBUG;
            long mask = (1 << index) - 1;
            this.mData = (this.mData & mask) | ((this.mData & (~mask)) << 1);
            if (value) {
                set(index);
            } else {
                clear(index);
            }
            if (lastBit || this.next != null) {
                ensureNext();
                this.next.insert(0, lastBit);
            }
        }

        boolean remove(int index) {
            if (index >= 64) {
                ensureNext();
                return this.next.remove(index - 64);
            }
            long mask = 1 << index;
            boolean value = (this.mData & mask) != 0 ? true : ChildHelper.DEBUG;
            this.mData &= ~mask;
            mask--;
            this.mData = (this.mData & mask) | Long.rotateRight(this.mData & (~mask), 1);
            if (this.next != null) {
                if (this.next.get(0)) {
                    set(63);
                }
                this.next.remove(0);
            }
            return value;
        }

        int countOnesBefore(int index) {
            if (this.next == null) {
                if (index >= 64) {
                    return Long.bitCount(this.mData);
                }
                return Long.bitCount(this.mData & ((1 << index) - 1));
            } else if (index < 64) {
                return Long.bitCount(this.mData & ((1 << index) - 1));
            } else {
                return this.next.countOnesBefore(index - 64) + Long.bitCount(this.mData);
            }
        }

        public String toString() {
            if (this.next == null) {
                return Long.toBinaryString(this.mData);
            }
            return this.next.toString() + "xx" + Long.toBinaryString(this.mData);
        }
    }

    interface Callback {
        void addView(View view, int i);

        void attachViewToParent(View view, int i, LayoutParams layoutParams);

        void detachViewFromParent(int i);

        View getChildAt(int i);

        int getChildCount();

        ViewHolder getChildViewHolder(View view);

        int indexOfChild(View view);

        void removeAllViews();

        void removeViewAt(int i);
    }

    ChildHelper(Callback callback) {
        this.mCallback = callback;
    }

    void addView(View child, boolean hidden) {
        addView(child, -1, hidden);
    }

    void addView(View child, int index, boolean hidden) {
        int offset;
        if (index < 0) {
            offset = this.mCallback.getChildCount();
        } else {
            offset = getOffset(index);
        }
        this.mCallback.addView(child, offset);
        this.mBucket.insert(offset, hidden);
        if (hidden) {
            this.mHiddenViews.add(child);
        }
    }

    private int getOffset(int index) {
        if (index < 0) {
            return -1;
        }
        int limit = this.mCallback.getChildCount();
        int offset = index;
        while (offset < limit) {
            int diff = index - (offset - this.mBucket.countOnesBefore(offset));
            if (diff == 0) {
                while (this.mBucket.get(offset)) {
                    offset++;
                }
                return offset;
            }
            offset += diff;
        }
        return -1;
    }

    void removeView(View view) {
        int index = this.mCallback.indexOfChild(view);
        if (index >= 0) {
            this.mCallback.removeViewAt(index);
            if (this.mBucket.remove(index)) {
                this.mHiddenViews.remove(view);
            }
        }
    }

    void removeViewAt(int index) {
        int offset = getOffset(index);
        View view = this.mCallback.getChildAt(offset);
        if (view != null) {
            this.mCallback.removeViewAt(offset);
            if (this.mBucket.remove(offset)) {
                this.mHiddenViews.remove(view);
            }
        }
    }

    View getChildAt(int index) {
        return this.mCallback.getChildAt(getOffset(index));
    }

    void removeAllViewsUnfiltered() {
        this.mCallback.removeAllViews();
        this.mBucket.reset();
        this.mHiddenViews.clear();
    }

    View findHiddenNonRemovedView(int position, int type) {
        int count = this.mHiddenViews.size();
        for (int i = 0; i < count; i++) {
            View view = (View) this.mHiddenViews.get(i);
            ViewHolder holder = this.mCallback.getChildViewHolder(view);
            if (holder.getPosition() == position && (holder.isInvalid() ^ 1) != 0 && (type == -1 || holder.getItemViewType() == type)) {
                return view;
            }
        }
        return null;
    }

    void attachViewToParent(View child, int index, LayoutParams layoutParams, boolean hidden) {
        int offset;
        if (index < 0) {
            offset = this.mCallback.getChildCount();
        } else {
            offset = getOffset(index);
        }
        this.mCallback.attachViewToParent(child, offset, layoutParams);
        this.mBucket.insert(offset, hidden);
    }

    int getChildCount() {
        return this.mCallback.getChildCount() - this.mHiddenViews.size();
    }

    int getUnfilteredChildCount() {
        return this.mCallback.getChildCount();
    }

    View getUnfilteredChildAt(int index) {
        return this.mCallback.getChildAt(index);
    }

    void detachViewFromParent(int index) {
        int offset = getOffset(index);
        this.mCallback.detachViewFromParent(offset);
        this.mBucket.remove(offset);
    }

    int indexOfChild(View child) {
        int index = this.mCallback.indexOfChild(child);
        if (index == -1 || this.mBucket.get(index)) {
            return -1;
        }
        return index - this.mBucket.countOnesBefore(index);
    }

    boolean isHidden(View view) {
        return this.mHiddenViews.contains(view);
    }

    void hide(View view) {
        int offset = this.mCallback.indexOfChild(view);
        if (offset < 0) {
            throw new IllegalArgumentException("view is not a child, cannot hide " + view);
        }
        this.mBucket.set(offset);
        this.mHiddenViews.add(view);
    }

    public String toString() {
        return this.mBucket.toString();
    }

    boolean removeViewIfHidden(View view) {
        int index = this.mCallback.indexOfChild(view);
        boolean remove;
        if (index == -1) {
            remove = this.mHiddenViews.remove(view);
            return true;
        } else if (!this.mBucket.get(index)) {
            return DEBUG;
        } else {
            this.mBucket.remove(index);
            this.mCallback.removeViewAt(index);
            remove = this.mHiddenViews.remove(view);
            return true;
        }
    }
}
