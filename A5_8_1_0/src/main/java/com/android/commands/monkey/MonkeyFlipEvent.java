package com.android.commands.monkey;

import android.app.IActivityManager;
import android.view.IWindowManager;
import java.io.FileOutputStream;
import java.io.IOException;

public class MonkeyFlipEvent extends MonkeyEvent {
    private static final byte[] FLIP_0 = new byte[]{Byte.MAX_VALUE, (byte) 6, (byte) 0, (byte) 0, (byte) -32, (byte) 57, (byte) 1, (byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] FLIP_1 = new byte[]{(byte) -123, (byte) 6, (byte) 0, (byte) 0, (byte) -97, (byte) -91, (byte) 12, (byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    private final boolean mKeyboardOpen;

    public MonkeyFlipEvent(boolean keyboardOpen) {
        super(5);
        this.mKeyboardOpen = keyboardOpen;
    }

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (verbose > 0) {
            Logger.out.println(":Sending Flip keyboardOpen=" + this.mKeyboardOpen);
        }
        try {
            FileOutputStream f = new FileOutputStream("/dev/input/event0");
            f.write(this.mKeyboardOpen ? FLIP_0 : FLIP_1);
            f.close();
            return 1;
        } catch (IOException e) {
            Logger.out.println("Got IOException performing flip" + e);
            return 0;
        }
    }
}
