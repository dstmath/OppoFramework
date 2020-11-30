package com.android.commands.monkey;

import android.app.IActivityManager;
import android.view.IWindowManager;
import java.io.FileOutputStream;
import java.io.IOException;

public class MonkeyFlipEvent extends MonkeyEvent {
    private static final byte[] FLIP_0 = {Byte.MAX_VALUE, 6, 0, 0, -32, 57, 1, 0, 5, 0, 0, 0, 1, 0, 0, 0};
    private static final byte[] FLIP_1 = {-123, 6, 0, 0, -97, -91, 12, 0, 5, 0, 0, 0, 0, 0, 0, 0};
    private final boolean mKeyboardOpen;

    public MonkeyFlipEvent(boolean keyboardOpen) {
        super(5);
        this.mKeyboardOpen = keyboardOpen;
    }

    @Override // com.android.commands.monkey.MonkeyEvent
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (verbose > 0) {
            Logger logger = Logger.out;
            logger.println(":Sending Flip keyboardOpen=" + this.mKeyboardOpen);
        }
        try {
            FileOutputStream f = new FileOutputStream("/dev/input/event0");
            f.write(this.mKeyboardOpen ? FLIP_0 : FLIP_1);
            f.close();
            return 1;
        } catch (IOException e) {
            Logger logger2 = Logger.out;
            logger2.println("Got IOException performing flip" + e);
            return 0;
        }
    }
}
