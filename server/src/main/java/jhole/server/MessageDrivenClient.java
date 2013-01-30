package jhole.server;

import jhole.messaging.MessageHandler;
import jhole.messaging.MessagingService;
import jhole.messaging.Messenger;
import jhole.streamcoding.Codec;
import jhole.streamcoding.ObjectStream;
import jhole.streamcoding.TransferModeBasedCodec;
import jhole.streamcoding.TransferMode;
import jhole.utils.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class MessageDrivenClient implements Client {
    private static final Logger logger = LoggerFactory.getLogger(MessageDrivenClient.class);
    private final Messenger messenger;
    private final BlockingQueue<Object> messages = new ArrayBlockingQueue<Object>(1024*64);
    private Runnable destroyListener;
    private final long creationTime = System.currentTimeMillis();

    public MessageDrivenClient(MessagingService messagingService, Runnable destroyListener) {
        this.destroyListener = destroyListener;
        messenger = messagingService.join(new MessageHandler() {
            @Override
            public void handleMessage(Object message) {
                logger.trace("add to downstream queue: {}", message);
                messages.offer(message);
            }
        });
    }

    @Override
    public  void handleUpstream(HttpServletRequest request, TransferMode transferMode) throws IOException {

        Codec codec = new TransferModeBasedCodec(transferMode);

        for (Object message: Iterators.iterateOnce(codec.read(request.getInputStream()))) {
            logger.trace("handle upstream {}", message);
            messenger.send(message);
        }
    }

    @Override
    public void handleDownstream(HttpServletResponse response, TransferMode transferMode, long timeout) throws IOException {

        Codec codec = new TransferModeBasedCodec(transferMode);
        ObjectStream objectStream = codec.write(response.getOutputStream());
        response.setContentType(transferMode.getContentType());
        while (true) {

            Object message;
            try {
                message = messages.poll(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            if (message == null) {
                break;
            }
            objectStream.write(message);
            response.getOutputStream().flush();
            logger.trace("handle downstream {}", message);
        }
    }

    @Override
    public void destroy() {
        try {
            destroyListener.run();
        } catch (Exception e) {
            //
        }
        messenger.destroy();
    }

    public int getQueueSize() {
        return messages.size();
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
