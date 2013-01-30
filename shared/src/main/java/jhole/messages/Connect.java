package jhole.messages;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class Connect {
    private String requestId;
    private String host;
    private int port;

    public Connect() {
    }

    public Connect(String requestId, String host, int port) {
        this.requestId = requestId;
        this.host = host;
        this.port = port;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Connect{" +
                "requestId='" + requestId + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
