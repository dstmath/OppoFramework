package com.android.server.am;

import com.android.server.am.ColorHansPackageSelector;

/* access modifiers changed from: package-private */
/* compiled from: ColorHansManager */
public interface IHansScene {
    ColorHansRestriction getHansRestriction();

    void hansFreeze();

    void hansFreeze(int i);

    void hansUnFreeze(int i, String str);

    void hansUnFreeze(String str);

    boolean isFreezed(int i);

    void onInit();

    void updateTargetMap(int i, ColorHansPackageSelector.HansPackage hansPackage);
}
