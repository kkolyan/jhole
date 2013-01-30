package jhole.connector;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public interface PeerListener {
    void handleData(byte[] bytes, int offset, int length);
    void handleDisconnect();
    void handleConnect(Peer peer);
    void handleReject(String message);
}
