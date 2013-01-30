package jhole.messaging.direct;

import jhole.messaging.MessageHandler;
import jhole.messaging.MessagingService;
import jhole.messaging.Messenger;
import jhole.messaging.direct.DeliveringMessenger;

import java.util.concurrent.Executor;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class DirectMessagingService implements MessagingService {
    private Executor executor;

    public DirectMessagingService(Executor executor) {
        this.executor = executor;
    }

    @Override
    public Messenger join(MessageHandler handler) {
        return new DeliveringMessenger(executor, handler);
    }
}
