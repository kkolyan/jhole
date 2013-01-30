package jhole.client;

import jhole.messaging.http.HttpMessagingService;
import jhole.connector.messagedriven.MessageDrivenConnector;

import java.io.IOException;

public class SimpleBootstrap extends Bootstrap {
    public static void main(String[] args) throws IOException {
        Acceptor acceptor = new Acceptor(new MessageDrivenConnector(new HttpMessagingService(
                executor, TransferModes.getCurrent())), executor);
        acceptor.run();
    }
}
