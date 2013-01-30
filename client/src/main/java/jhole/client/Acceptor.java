package jhole.client;

import jhole.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

public class Acceptor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Acceptor.class);

    private ServerSocket serverSocket;
    private Connector connector;
    private Executor executor;

    public Acceptor(Connector connector, Executor executor) throws IOException {
        this.connector = connector;
        this.executor = executor;
        serverSocket = new ServerSocket(Integer.getInteger("jhole.client.port"));
    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                executor.execute(new IncomingConnectionProcessingJob(socket, connector, Integer.getInteger("jhole.client.transferBufferSize", 1024), executor));
            } catch (Exception e) {
                logger.error(e.toString(), e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    //
                }
            }
        }
    }
}
