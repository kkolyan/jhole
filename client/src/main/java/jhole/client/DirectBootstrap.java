package jhole.client;

import jhole.connector.direct.DirectConnector;

import java.io.IOException;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class DirectBootstrap extends Bootstrap {
    public static void main(String[] args) throws IOException {
        Acceptor acceptor = new Acceptor(new DirectConnector(executor), executor);
        acceptor.run();
    }
}
