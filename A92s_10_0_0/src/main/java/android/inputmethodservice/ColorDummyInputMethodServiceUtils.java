package android.inputmethodservice;

import android.app.Dialog;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;

public class ColorDummyInputMethodServiceUtils implements IColorInputMethodServiceUtils {
    @Override // android.inputmethodservice.IColorInputMethodServiceUtils
    public void init(Context context) {
    }

    @Override // android.inputmethodservice.IColorInputMethodServiceUtils
    public void beforeInputShow() {
    }

    @Override // android.inputmethodservice.IColorInputMethodServiceUtils
    public void afterInputShow() {
    }

    @Override // android.inputmethodservice.IColorInputMethodServiceUtils
    public boolean getDockSide() {
        return false;
    }

    @Override // android.inputmethodservice.IColorInputMethodServiceUtils
    public void onChange(Uri uri) {
    }

    @Override // android.inputmethodservice.IColorInputMethodServiceUtils
    public void updateColorNavigationGuardColor(Dialog window) {
    }

    @Override // android.inputmethodservice.IColorInputMethodServiceUtils
    public void updateColorNavigationGuardColorDelay(Dialog window) {
    }

    @Override // android.inputmethodservice.IColorInputMethodServiceUtils
    public void onComputeRaise(InputMethodService.Insets mTmpInsets, Dialog window) {
    }

    @Override // android.inputmethodservice.IColorInputMethodServiceUtils
    public void uploadData(long time) {
    }
}
