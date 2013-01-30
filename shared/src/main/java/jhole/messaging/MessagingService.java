package jhole.messaging;

public interface MessagingService {
    Messenger join(MessageHandler handler);
}
