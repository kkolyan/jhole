package jhole.messages;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class Reject {
    private String requestId;
    private String message;

    public Reject() {
    }

    public Reject(String requestId, String message) {
        this.requestId = requestId;
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Reject{" +
                "requestId='" + requestId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
