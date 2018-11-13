package org.gsma.joyn;

public interface JoynServiceListener {
    void onServiceConnected();

    void onServiceDisconnected(int i);
}
