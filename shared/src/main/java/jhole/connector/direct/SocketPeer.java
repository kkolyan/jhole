package jhole.connector.direct;

import jhole.connector.Address;
import jhole.connector.Peer;
import jhole.connector.PeerListener;
import jhole.history.HistoryEntry;
import jhole.utils.CountingOutputStream;
import jhole.utils.LoggingInputStream;
import jhole.utils.LoggingOutputStream;
import jhole.history.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class SocketPeer implements Peer {
    private static final Logger logger = LoggerFactory.getLogger(SocketPeer.class);
    private Socket socket;
    private Executor executor;
    private OutputStream outputStream;
    private InputStream inputStream;

    private static final AtomicLong counter = new AtomicLong();
    private final long id = counter.incrementAndGet();
    private PeerListener listener;
    private long openedTime;
    private AtomicLong readBytes = new AtomicLong();
    private AtomicLong writtenBytes = new AtomicLong();

    public SocketPeer(Address address, Executor executor, PeerListener listener) {
        this.listener = listener;
        this.executor = executor;
        try {
            this.socket = new Socket(address.getHost(), address.getPort());
            openedTime = System.currentTimeMillis();
            init();
            listener.handleConnect(this);
        } catch (IOException e) {
            logger.trace("rejected: {} because {}", address, e);
            listener.handleReject(e.toString());
        }
    }

    private void init() throws IOException {

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        if (History.getInstance() != null) {
            HistoryEntry historyEntry = History.getInstance().addEntry("SocketPeer");

            inputStream = new LoggingInputStream(inputStream,
                    historyEntry.createSection("in").getOutputStream());

            outputStream = new LoggingOutputStream(outputStream,
                    historyEntry.createSection("out").getOutputStream());
        }

        inputStream = new LoggingInputStream(inputStream, new CountingOutputStream(readBytes));
        outputStream = new LoggingOutputStream(outputStream, new CountingOutputStream(writtenBytes));

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    byte [] bytes = new byte[1024];
                    while (!socket.isClosed()) {
                        int n;
                        try {
                            n = inputStream.read(bytes);
                        } catch (IOException e) {
                            n = -1;
                        }
                        if (n < 0) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                //
                            }
                            break;
                        }
                        listener.handleData(bytes, 0, n);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    listener.handleDisconnect();
                }
            }
        });
    }

    @Override
    public void send(byte[] bytes, int offset, int length) {
        final byte[] copy = Arrays.copyOfRange(bytes, offset, length);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.write(copy);
                } catch (IOException e) {
                    logger.trace(e.toString(), e);
                }
            }
        });
    }

    @Override
    public void disconnect() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.trace(e.toString(), e);
                }
            }
        });
    }

    public Socket getSocket() {
        return socket;
    }

    public long getOpenedTime() {
        return openedTime;
    }

    public long getReadBytes() {
        return readBytes.get();
    }

    public long getWrittenBytes() {
        return writtenBytes.get();
    }
}
