package jhole.messaging.direct;

import jhole.messages.*;
import jhole.messaging.Messenger;
import jhole.connector.Address;
import jhole.connector.Connector;
import jhole.connector.Peer;
import jhole.connector.PeerListener;
import jhole.connector.direct.DirectConnector;
import jhole.messaging.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DeliveringMessenger implements Messenger {
    private static final Logger logger = LoggerFactory.getLogger(DeliveringMessenger.class);
    private MessageHandler messageHandler;
    private AtomicLong connectionCounter = new AtomicLong();
    private Map<Long,Peer> connections = new ConcurrentHashMap<Long, Peer>();
    private final Connector connector;

    public DeliveringMessenger(Executor executor, MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        this.connector = new DirectConnector(executor);
    }

    @Override
    public void send(Object message) {
        logger.trace("send {}", message);

        if (message instanceof Connect) {
            final Connect connect = (Connect) message;
            final long connectionId = connectionCounter.incrementAndGet();
            connector.connect(new Address(connect.getHost(), connect.getPort()), new PeerListener() {
                @Override
                public void handleData(byte[] bytes, int offset, int length) {
                    final byte[] copy = Arrays.copyOfRange(bytes, offset, length);
                    messageHandler.handleMessage(new Transfer(connectionId, copy));
                }

                @Override
                public void handleDisconnect() {
                    connections.remove(connectionId);
                }

                @Override
                public void handleConnect(Peer peer) {
                    messageHandler.handleMessage(new Accept(connect.getRequestId(), connectionId));
                    connections.put(connectionId, peer);
                }

                @Override
                public void handleReject(String message) {
                    messageHandler.handleMessage(new Reject(connect.getRequestId(), message));
                }
            });
        }
        else if (message instanceof Disconnect) {
            long connectionId = ((Disconnect) message).getConnectionId();
            Peer peer = connections.remove(connectionId);
            if (peer != null) {
                try {
                    peer.disconnect();
                } catch (Exception e) {
                    //
                }
            }
        }
        else if (message instanceof Transfer) {
            Transfer transfer = (Transfer) message;
            Peer peer = connections.get(transfer.getConnectionId());
            if (peer != null) {
                try {
                    byte[] data = transfer.getData();
                    peer.send(data, 0, data.length);
                } catch (Exception e) {
                    messageHandler.handleMessage(new ErrorMessage(e.toString()));
                }
            }
        }
        else if (message instanceof ErrorMessage) {
            logger.warn(((ErrorMessage) message).getMessage());
        }
        else throw new IllegalStateException("unsupported command: "+message);
    }

    @Override
    public void destroy() {
        for (Iterator<Peer> iterator = connections.values().iterator(); iterator.hasNext(); ) {
            Peer peer = iterator.next();
            try {
                peer.disconnect();
            } catch (Exception e) {
                //
            }
            iterator.remove();
        }
    }

    public Map<Long, Peer> getConnections() {
        return connections;
    }
}
