package com.mediatek.imsplugin;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.mediatek.ims.ImsCallSessionProxy;
import com.mediatek.ims.ImsService;
import com.mediatek.ims.plugin.impl.ExtensionPluginFactoryBase;
import com.mediatek.ims.plugin.impl.ImsCallPluginBase;
import com.mediatek.ims.plugin.impl.ImsSSExtPluginBase;
import com.mediatek.ims.plugin.impl.ImsSelfActivatorBase;
import com.mediatek.ims.ril.ImsCommandsInterface;

public class ExtensionPluginFactoryImpl extends ExtensionPluginFactoryBase {
    public ImsCallPluginBase makeImsCallPlugin(Context context) {
        Log.d("ExtensionPluginFactoryImpl", "makeImsCallPlugin()");
        return new ImsCallPluginImpl(context);
    }

    public ImsSelfActivatorBase makeImsSelfActivator(Context context, Handler handler, ImsCallSessionProxy callSessionProxy, ImsCommandsInterface imsRILAdapter, ImsService imsService, int phoneId) {
        Log.d("ExtensionPluginFactoryImpl", "makeImsSelfActivator()");
        return new ImsSelfActivatorImpl(context, handler, callSessionProxy, imsRILAdapter, imsService, phoneId);
    }

    public ImsSSExtPluginBase makeImsSSExtPlugin(Context context) {
        Log.d("ExtensionPluginFactoryImpl", "makeImsSSExtPlugin()");
        return new ImsSSExtPluginImpl(context);
    }
}
