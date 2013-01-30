package jhole.messaging;

public interface Messenger {
    void send(Object message);
    void destroy();
}
