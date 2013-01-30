package jhole.client;

import jhole.history.HistoryEntry;
import jhole.utils.LineReader;
import jhole.connector.PeerListener;
import jhole.utils.LoggingInputStream;
import jhole.utils.LoggingOutputStream;
import jhole.connector.Address;
import jhole.connector.Connector;
import jhole.connector.Peer;
import jhole.history.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class IncomingConnectionProcessingJob implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(IncomingConnectionProcessingJob.class);
    private Socket socket;
    private Connector connector;
    private int bufferSize;
    private Executor executor;
    private final BlockingQueue<Runnable> downstreamEvents = new ArrayBlockingQueue<Runnable>(1024);
    private volatile boolean downstreamEventsTerminated;

    private static AtomicLong counter = new AtomicLong();
    private final long id = counter.incrementAndGet();

    private InputStream inputStream;
    private OutputStream outputStream;

    public IncomingConnectionProcessingJob(Socket socket, Connector connector, int bufferSize, Executor executor) {
        this.socket = socket;
        this.connector = connector;
        this.bufferSize = bufferSize;
        this.executor = executor;
    }

    @Override
    public void run() {

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            if (History.getInstance() != null) {

                HistoryEntry historyEntry = History.getInstance().addEntry("Incoming");
                inputStream = new LoggingInputStream(inputStream,
                        historyEntry.createSection("in").getOutputStream());

                outputStream = new LoggingOutputStream(outputStream,
                        historyEntry.createSection("out").getOutputStream());
            }

            final LineReader reader = new LineReader(inputStream);

            HttpRequest request;
            try {
                request = HttpRequest.parseRequest(reader);
            } catch (Exception e) {
                respondSilently(outputStream, "400 Bad Request");
                throw e;
            }

            try {
                if (request.getMethod().equals("CONNECT")) {
                    Address address = Address.parseAddress(request.getAddress());

                    connector.connect(address, new PeerListenerImpl(outputStream, reader));

                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (true) {
                                    Runnable task = downstreamEvents.poll(1000, TimeUnit.MILLISECONDS);
                                    if (task != null) {
                                        task.run();
                                    } else if (downstreamEventsTerminated) {
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                logger.error(e.toString(), e);
                            }
                        }
                    });
                } else {
                    respondSilently(outputStream, "405 Method Not Allowed");
                }
            } catch (Exception e) {
                respondSilently(outputStream, "500 Internal Server Error");
                throw e;
            }

        } catch (Exception e) {
            logger.error(e.toString(), e);
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    private void respondSilently(OutputStream outputStream, String status) {
        try {
            outputStream.write(("HTTP/1.1 " + status + "\r\n\r\n").getBytes());
        } catch (Exception e) {
            //
        }
    }

    private class PeerListenerImpl implements PeerListener {

        private final OutputStream outputStream;
        private final LineReader reader;

        public PeerListenerImpl(OutputStream outputStream, LineReader reader) {
            this.outputStream = outputStream;
            this.reader = reader;
        }

        @Override
        public void handleData(byte[] bytes, int offset, int length) {
            final byte[] copy = Arrays.copyOfRange(bytes, offset, length);
            downstreamEvents.offer(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStream.write(copy);
                    } catch (IOException e) {
                        logger.trace("error writing: {}", e.toString());
                    }
                }
            });
        }

        @Override
        public void handleDisconnect() {
            downstreamEvents.offer(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        logger.trace("error closing: {}", e);
                    }
                    downstreamEventsTerminated = true;
                }
            });
        }

        @Override
        public void handleConnect(final Peer peer) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStream.write("HTTP/1.1 200 Connection established\r\n\r\n".getBytes());

                        byte[] rem = reader.getRemainder();
                        peer.send(rem, 0, rem.length);

                        byte[] buffer = new byte[bufferSize];
                        while (true) {
                            int n;
                            try {
                                n = reader.getStream().read(buffer);
                            } catch (IOException e) {
                                break;
                            }
                            if (n < 0) {
                                break;
                            }
                            peer.send(buffer, 0, n);
                        }
                    } catch (IOException e) {
                        logger.warn(e.toString(), e);
                    } finally {
                        peer.disconnect();
                    }
                }
            });
        }

        @Override
        public void handleReject(String message) {
            logger.trace("rejected due to {}", message);
            downstreamEvents.offer(new Runnable() {
                @Override
                public void run() {
                    respondSilently(outputStream, "404 Not Found");
                }
            });
        }
    }
}
