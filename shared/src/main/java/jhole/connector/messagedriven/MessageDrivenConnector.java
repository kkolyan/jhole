package jhole.connector.messagedriven;

import jhole.connector.Address;
import jhole.connector.Connector;
import jhole.connector.PeerListener;
import jhole.messages.*;
import jhole.messaging.MessageHandler;
import jhole.messaging.MessagingService;
import jhole.messaging.Messenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class MessageDrivenConnector implements Connector {
    private static final Logger logger = LoggerFactory.getLogger(MessageDrivenConnector.class);
    private Map<String,Connection> pendingConnections = new ConcurrentHashMap<String, Connection>();
    private Map<Long,PeerListener> activeConnections = new ConcurrentHashMap<Long, PeerListener>();
    private Messenger messenger;

    public MessageDrivenConnector(MessagingService messagingService) {
        messenger = messagingService.join(new MessageHandler() {
            @Override
            public void handleMessage(Object message) {
                logger.trace("handle {}", message);
                if (message instanceof Accept) {
                    Accept accept = (Accept) message;
                    Connection request = pendingConnections.remove(accept.getRequestId());
                    if (request != null) {
                        request.handleConnect(accept.getConnectionId());
                    }
                }
                else if (message instanceof Reject) {
                    Reject reject = (Reject) message;
                    Connection request = pendingConnections.remove(reject.getRequestId());
                    if (request != null) {
                        request.handleReject(reject.getMessage());
                    }
                }
                else if (message instanceof Disconnect) {
                    Disconnect disconnect = (Disconnect) message;
                    final PeerListener localPeer = activeConnections.remove(disconnect.getConnectionId());
                    if (localPeer != null) {
                        localPeer.handleDisconnect();
                    }
                }
                else if (message instanceof Transfer) {
                    Transfer transfer = (Transfer) message;
                    PeerListener localPeer = activeConnections.get(transfer.getConnectionId());
                    if (localPeer != null) {
                        byte[] data = transfer.getData();
                        localPeer.handleData(data, 0, data.length);
                    }
                }
                else if (message instanceof ErrorMessage) {
                    String error = ((ErrorMessage) message).getMessage();
                    logger.error(error);
                }
                else throw new IllegalStateException("unknown message: " + message);
            }
        });
    }

    @Override
    public void connect(Address address, PeerListener listener) {
        Connection request = new Connection(listener);
        pendingConnections.put(request.getRequestId(), request);

        messenger.send(new Connect(request.getRequestId(), address.getHost(), address.getPort()));
    }

    private class Connection {

        private final String requestId = UUID.randomUUID().toString();
        private final PeerListener listener;

        public Connection(PeerListener listener) {
            this.listener = listener;
        }

        public void handleConnect(long connectionId) {
            activeConnections.put(connectionId, listener);
            listener.handleConnect(new MessageDrivenPeer(messenger, connectionId));
        }

        public void handleReject(String message) {
            listener.handleReject(message);
        }

        public String getRequestId() {
            return requestId;
        }
    }

}
