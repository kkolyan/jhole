package jhole.connector.messagedriven;

import jhole.connector.Peer;
import jhole.messaging.Messenger;
import jhole.messages.Disconnect;
import jhole.messages.Transfer;

import java.util.Arrays;

/**
* @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
*/
public class MessageDrivenPeer implements Peer {
    private Messenger messenger;
    private long connectionId;

    public MessageDrivenPeer(Messenger messenger, long connectionId) {
        this.messenger = messenger;
        this.connectionId = connectionId;
    }

    @Override
    public void send(byte[] bytes, int offset, int length) {
        final byte[] copy = Arrays.copyOfRange(bytes, offset, offset + length);
        messenger.send(new Transfer(connectionId, copy));
    }

    @Override
    public void disconnect() {
        messenger.send(new Disconnect(connectionId));
    }
}
