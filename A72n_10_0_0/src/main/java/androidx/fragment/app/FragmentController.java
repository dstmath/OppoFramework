package androidx.fragment.app;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class FragmentController {
    private final FragmentHostCallback<?> mHost;

    public static FragmentController createController(FragmentHostCallback<?> callbacks) {
        return new FragmentController(callbacks);
    }

    private FragmentController(FragmentHostCallback<?> callbacks) {
        this.mHost = callbacks;
    }

    public FragmentManager getSupportFragmentManager() {
        return this.mHost.getFragmentManagerImpl();
    }

    public Fragment findFragmentByWho(String who) {
        return this.mHost.mFragmentManager.findFragmentByWho(who);
    }

    public void attachHost(Fragment parent) {
        this.mHost.mFragmentManager.attachController(this.mHost, this.mHost, parent);
    }

    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return this.mHost.mFragmentManager.onCreateView(parent, name, context, attrs);
    }

    public void noteStateNotSaved() {
        this.mHost.mFragmentManager.noteStateNotSaved();
    }

    public Parcelable saveAllState() {
        return this.mHost.mFragmentManager.saveAllState();
    }

    public void restoreAllState(Parcelable state, FragmentManagerNonConfig nonConfig) {
        this.mHost.mFragmentManager.restoreAllState(state, nonConfig);
    }

    public FragmentManagerNonConfig retainNestedNonConfig() {
        return this.mHost.mFragmentManager.retainNonConfig();
    }

    public void dispatchCreate() {
        this.mHost.mFragmentManager.dispatchCreate();
    }

    public void dispatchActivityCreated() {
        this.mHost.mFragmentManager.dispatchActivityCreated();
    }

    public void dispatchStart() {
        this.mHost.mFragmentManager.dispatchStart();
    }

    public void dispatchResume() {
        this.mHost.mFragmentManager.dispatchResume();
    }

    public void dispatchPause() {
        this.mHost.mFragmentManager.dispatchPause();
    }

    public void dispatchStop() {
        this.mHost.mFragmentManager.dispatchStop();
    }

    public void dispatchReallyStop() {
        this.mHost.mFragmentManager.dispatchReallyStop();
    }

    public void dispatchDestroy() {
        this.mHost.mFragmentManager.dispatchDestroy();
    }

    public void dispatchMultiWindowModeChanged(boolean isInMultiWindowMode) {
        this.mHost.mFragmentManager.dispatchMultiWindowModeChanged(isInMultiWindowMode);
    }

    public void dispatchPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        this.mHost.mFragmentManager.dispatchPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    public void dispatchConfigurationChanged(Configuration newConfig) {
        this.mHost.mFragmentManager.dispatchConfigurationChanged(newConfig);
    }

    public void dispatchLowMemory() {
        this.mHost.mFragmentManager.dispatchLowMemory();
    }

    public boolean dispatchCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        return this.mHost.mFragmentManager.dispatchCreateOptionsMenu(menu, inflater);
    }

    public boolean dispatchPrepareOptionsMenu(Menu menu) {
        return this.mHost.mFragmentManager.dispatchPrepareOptionsMenu(menu);
    }

    public boolean dispatchOptionsItemSelected(MenuItem item) {
        return this.mHost.mFragmentManager.dispatchOptionsItemSelected(item);
    }

    public boolean dispatchContextItemSelected(MenuItem item) {
        return this.mHost.mFragmentManager.dispatchContextItemSelected(item);
    }

    public void dispatchOptionsMenuClosed(Menu menu) {
        this.mHost.mFragmentManager.dispatchOptionsMenuClosed(menu);
    }

    public boolean execPendingActions() {
        return this.mHost.mFragmentManager.execPendingActions();
    }
}
