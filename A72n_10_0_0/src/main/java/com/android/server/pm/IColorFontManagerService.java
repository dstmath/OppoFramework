package com.android.server.pm;

import com.color.font.IColorBaseFontManager;

public interface IColorFontManagerService extends IColorBaseFontManager {
    void init(IColorPackageManagerServiceEx iColorPackageManagerServiceEx);

    void systemReady();
}
