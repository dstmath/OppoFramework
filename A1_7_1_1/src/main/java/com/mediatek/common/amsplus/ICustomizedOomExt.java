package com.mediatek.common.amsplus;

import com.mediatek.common.PluginImpl;

@PluginImpl(interfaceName = "com.mediatek.common.amsplus.ICustomizedOomExt")
public interface ICustomizedOomExt {
    int getCustomizedAdj(String str);
}
