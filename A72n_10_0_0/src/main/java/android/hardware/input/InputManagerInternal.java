package android.hardware.input;

import android.hardware.display.DisplayViewport;
import android.view.InputEvent;
import java.util.List;

public abstract class InputManagerInternal {
    public abstract boolean injectInputEvent(InputEvent inputEvent, int i);

    public abstract void setDisplayViewports(List<DisplayViewport> list);

    public abstract void setInteractive(boolean z);

    public abstract void setPulseGestureEnabled(boolean z);

    public abstract void toggleCapsLock(int i);
}
