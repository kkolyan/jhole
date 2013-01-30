package jhole.messages;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class Accept {
    private String requestId;
    private long connectionId;

    public Accept() {
    }

    public Accept(String requestId, long connectionId) {
        this.requestId = requestId;
        this.connectionId = connectionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(long connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public String toString() {
        return "Accept{" +
                "requestId='" + requestId + '\'' +
                ", connectionId=" + connectionId +
                '}';
    }
}
