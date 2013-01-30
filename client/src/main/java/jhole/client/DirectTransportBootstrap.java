package jhole.client;

import jhole.messaging.direct.DirectMessagingService;
import jhole.connector.messagedriven.MessageDrivenConnector;

import java.io.IOException;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class DirectTransportBootstrap extends Bootstrap {

    public static void main(String[] args) throws IOException {
        Acceptor acceptor = new Acceptor(new MessageDrivenConnector(new DirectMessagingService(executor)), executor);
        acceptor.run();
    }
}
