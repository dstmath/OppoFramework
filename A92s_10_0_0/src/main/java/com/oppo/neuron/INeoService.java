package com.oppo.neuron;

public interface INeoService {
    public static final int AI_APP_PRELOAD_PREDICT = 302;
    public static final String DESCRIPTOR = "neoservice";

    String[] appPreloadPredict();
}
