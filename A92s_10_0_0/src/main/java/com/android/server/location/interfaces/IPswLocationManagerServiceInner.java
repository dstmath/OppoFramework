package com.android.server.location.interfaces;

import android.os.Handler;
import java.util.ArrayList;

public interface IPswLocationManagerServiceInner {
    ArrayList<String> getGpsPackageNames();

    Handler getHandler();
}
