package android.support.v13.app;

import android.app.Fragment;
import android.app.Fragment.SavedState;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

public abstract class FragmentStatePagerAdapter extends PagerAdapter {
    private static final boolean DEBUG = false;
    private static final String TAG = "FragmentStatePagerAdapter";
    private FragmentTransaction mCurTransaction = null;
    private Fragment mCurrentPrimaryItem = null;
    private final FragmentManager mFragmentManager;
    private ArrayList<Fragment> mFragments = new ArrayList();
    private ArrayList<SavedState> mSavedState = new ArrayList();

    public abstract Fragment getItem(int i);

    public FragmentStatePagerAdapter(FragmentManager fm) {
        this.mFragmentManager = fm;
    }

    public void startUpdate(ViewGroup container) {
    }

    public Object instantiateItem(ViewGroup container, int position) {
        if (this.mFragments.size() > position) {
            Fragment f = (Fragment) this.mFragments.get(position);
            if (f != null) {
                return f;
            }
        }
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }
        Fragment fragment = getItem(position);
        if (this.mSavedState.size() > position) {
            SavedState fss = (SavedState) this.mSavedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            }
        }
        while (this.mFragments.size() <= position) {
            this.mFragments.add(null);
        }
        FragmentCompat.setMenuVisibility(fragment, DEBUG);
        FragmentCompat.setUserVisibleHint(fragment, DEBUG);
        this.mFragments.set(position, fragment);
        this.mCurTransaction.add(container.getId(), fragment);
        return fragment;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (this.mCurTransaction == null) {
            this.mCurTransaction = this.mFragmentManager.beginTransaction();
        }
        while (this.mSavedState.size() <= position) {
            this.mSavedState.add(null);
        }
        this.mSavedState.set(position, this.mFragmentManager.saveFragmentInstanceState(fragment));
        this.mFragments.set(position, null);
        this.mCurTransaction.remove(fragment);
    }

    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != this.mCurrentPrimaryItem) {
            if (this.mCurrentPrimaryItem != null) {
                FragmentCompat.setMenuVisibility(this.mCurrentPrimaryItem, DEBUG);
                FragmentCompat.setUserVisibleHint(this.mCurrentPrimaryItem, DEBUG);
            }
            if (fragment != null) {
                FragmentCompat.setMenuVisibility(fragment, true);
                FragmentCompat.setUserVisibleHint(fragment, true);
            }
            this.mCurrentPrimaryItem = fragment;
        }
    }

    public void finishUpdate(ViewGroup container) {
        if (this.mCurTransaction != null) {
            this.mCurTransaction.commitAllowingStateLoss();
            this.mCurTransaction = null;
            this.mFragmentManager.executePendingTransactions();
        }
    }

    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view ? true : DEBUG;
    }

    public Parcelable saveState() {
        Parcelable state = null;
        if (this.mSavedState.size() > 0) {
            state = new Bundle();
            SavedState[] fss = new SavedState[this.mSavedState.size()];
            this.mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        for (int i = 0; i < this.mFragments.size(); i++) {
            Fragment f = (Fragment) this.mFragments.get(i);
            if (f != null) {
                if (state == null) {
                    state = new Bundle();
                }
                this.mFragmentManager.putFragment(state, "f" + i, f);
            }
        }
        return state;
    }

    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            this.mSavedState.clear();
            this.mFragments.clear();
            if (fss != null) {
                for (Parcelable parcelable : fss) {
                    this.mSavedState.add((SavedState) parcelable);
                }
            }
            for (String key : bundle.keySet()) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = this.mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while (this.mFragments.size() <= index) {
                            this.mFragments.add(null);
                        }
                        FragmentCompat.setMenuVisibility(f, DEBUG);
                        this.mFragments.set(index, f);
                    } else {
                        Log.w(TAG, "Bad fragment at key " + key);
                    }
                }
            }
        }
    }
}
