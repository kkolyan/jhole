package jhole.connector.direct;

import jhole.connector.Address;
import jhole.connector.Connector;
import jhole.connector.PeerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class DirectConnector implements Connector {
    private Executor executor;
    private static final Logger logger = LoggerFactory.getLogger(DirectConnector.class);

    public DirectConnector(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void connect(Address address, PeerListener listener) {
        new SocketPeer(address, executor, listener);
    }
}
