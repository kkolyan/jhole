package jhole.connector;

public interface Connector {
    void connect(Address address, PeerListener listener);
}
