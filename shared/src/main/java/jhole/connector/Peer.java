package jhole.connector;

public interface Peer {
    void send(byte[] bytes, int offset, int length);
    void disconnect();
}
