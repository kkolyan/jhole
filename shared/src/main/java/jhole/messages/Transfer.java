package jhole.messages;

import java.util.Arrays;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class Transfer {
    private long connectionId;
    private byte[] data;

    public Transfer(long connectionId, byte[] data) {
        this.connectionId = connectionId;
        this.data = data;
    }

    public Transfer() {
    }

    public long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(long connectionId) {
        this.connectionId = connectionId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
//        String s = new String(data);
//        int lb = s.indexOf('\n');
//        if (lb < 50 && lb > 0) {
//            s = s.substring(0, lb)+"...";
//        }
//        else if (s.length() > 50) {
//            s = s.substring(0, 50)+"...";
//        }
        return "Transfer{" +
                "connectionId=" + connectionId +
                ", data=" + data.length +
                '}';
    }
}
