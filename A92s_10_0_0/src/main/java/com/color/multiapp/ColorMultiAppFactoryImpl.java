package com.color.multiapp;

public class ColorMultiAppFactoryImpl extends ColorMultiAppFactory {
    public IColorMultiApp getColorMultiApp() {
        return new ColorMultiAppImpl();
    }
}
