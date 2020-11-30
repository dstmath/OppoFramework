package com.color.inner.hardware.input;

import android.hardware.input.InputManager;
import android.view.InputEvent;

public class InputManagerWrapper {
    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;

    private InputManagerWrapper() {
    }

    public static boolean injectInputEvent(InputEvent ev, int mode) {
        return InputManager.getInstance().injectInputEvent(ev, mode);
    }
}
