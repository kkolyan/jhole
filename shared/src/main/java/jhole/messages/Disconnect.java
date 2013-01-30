package jhole.messages;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class Disconnect {
    private long connectionId;

    public Disconnect() {
    }

    public Disconnect(long connectionId) {
        this.connectionId = connectionId;
    }

    public long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(long connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public String toString() {
        return "Disconnect{" +
                "connectionId=" + connectionId +
                '}';
    }
}
