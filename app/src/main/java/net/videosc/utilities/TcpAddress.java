package net.videosc.utilities;

import oscP5.OscP5;

public class TcpAddress {
    final private String mAddress;
    private final int mPort;

    public TcpAddress(String address, int port) {
        this.mAddress = address;
        this.mPort = port;
    }

    public String address() {
        return this.mAddress;
    }

    public int port() {
        return this.mPort;
    }

    public int protocol() {
        return OscP5.TCP;
    }
}
