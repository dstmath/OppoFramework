package com.mediatek.common.media;

import android.content.Context;
import android.net.Uri;
import java.util.Map;

public interface IOmaSettingHelper {
    Map<String, String> setSettingHeader(Context context, Uri uri, Map<String, String> map);
}
