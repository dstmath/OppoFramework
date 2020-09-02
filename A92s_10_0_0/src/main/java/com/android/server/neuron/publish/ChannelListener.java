package com.android.server.neuron.publish;

import android.net.ConnectivityManager;
import android.os.Parcel;
import android.util.Slog;
import com.android.server.neuron.publish.Channel;
import com.android.server.neuron.publish.Response;
import com.oppo.neuron.NeuronSystemManager;

public final class ChannelListener implements Channel.ChannelEventListener {
    private final String TAG = "NeuronSystem";
    private IndicationHandler mIndicationHandler;

    public void setIndicationHandler(IndicationHandler h) {
        this.mIndicationHandler = h;
    }

    @Override // com.android.server.neuron.publish.Channel.ChannelEventListener
    public void onConnection(Channel.RequestSender sender) {
        String app = NeuronContext.getSystemStatus().getForegroundApp();
        if (app != null) {
            Request req = Request.obtain();
            Parcel parcel = req.prepare();
            parcel.writeInt(1);
            parcel.writeString(app);
            parcel.writeInt(1);
            parcel.writeString("version");
            req.commit();
            sender.sendRequest(req);
        }
        int type = NeuronContext.getSystemStatus().getNetworkType();
        if (ConnectivityManager.isNetworkTypeValid(type)) {
            Request req2 = Request.obtain();
            Parcel parcel2 = req2.prepare();
            parcel2.writeInt(7);
            parcel2.writeInt(type);
            parcel2.writeInt(1);
            parcel2.writeString(NeuronContext.getSystemStatus().getIfaceName());
            if (type == 1) {
                String ssid = NeuronContext.getSystemStatus().getWifissid();
                String bssid = NeuronContext.getSystemStatus().getWifiBssid();
                parcel2.writeString(ssid);
                parcel2.writeString(bssid);
                parcel2.writeInt(0);
            }
            req2.commit();
            sender.sendRequest(req2);
        }
    }

    @Override // com.android.server.neuron.publish.Channel.ChannelEventListener
    public void onError(int error) {
        Slog.e("NeuronSystem", "ChannelListener  onError err:" + error);
    }

    @Override // com.android.server.neuron.publish.Channel.ChannelEventListener
    public void onResponse(Request req, Response.NativeResponse resp) {
        if (NeuronSystemManager.LOG_ON) {
            Slog.d("NeuronSystem", "ChannelListener  onResponse resp:" + resp.toString());
            long us = (System.nanoTime() / 1000) - req.getTimeStamp();
            Slog.d("NeuronSystem", "ChannelListener  request-> response consume time:" + (us / 1000) + "." + (us % 1000) + "ms");
        }
    }

    @Override // com.android.server.neuron.publish.Channel.ChannelEventListener
    public void onIndication(Response.NativeIndication indication) {
        if (NeuronSystemManager.LOG_ON) {
            Slog.d("NeuronSystem", "ChannelListener  onIndication indication:" + indication.toString());
        }
        IndicationHandler indicationHandler = this.mIndicationHandler;
        if (indicationHandler != null) {
            indicationHandler.handle(indication);
        }
    }
}
