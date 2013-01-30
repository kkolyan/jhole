package jhole.server;

import jhole.messaging.MessagingService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class ClientManager {
    private Map<String,Client> clients = new ConcurrentHashMap<String, Client>();
    private MessagingService messagingService;

    public ClientManager(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public Client get(String id) {
        return clients.get(id);
    }

    public String create() {
        final String id = UUID.randomUUID().toString();
        clients.put(id, new MessageDrivenClient(messagingService, new Runnable() {
            @Override
            public void run() {
                clients.remove(id);
            }
        }));
        return id;
    }

    public Map<String, Client> getClients() {
        return clients;
    }

    public void destroy() {
        for (Client client: clients.values()) {
            try {
                client.destroy();
            } catch (Exception e) {
                //
            }
        }
    }
}
