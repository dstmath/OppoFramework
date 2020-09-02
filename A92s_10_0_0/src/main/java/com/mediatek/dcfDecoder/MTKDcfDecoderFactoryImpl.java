package com.mediatek.dcfDecoder;

public class MTKDcfDecoderFactoryImpl extends MTKDcfDecoderFactory {
    public MTKDcfDecoderManager makeMTKDcfDecoderManager() {
        return new MTKDcfDecoderManagerImpl();
    }
}
