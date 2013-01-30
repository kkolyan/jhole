package jhole.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {
    public static void pump(InputStream from, OutputStream to) throws IOException {
        byte[] bytes = new byte[1024];
        int pumped = 0;
        while (true) {
            int n = from.read(bytes);
            if (n <= 0) {
                break;
            }
            pumped += n;
            to.write(bytes, 0, n);
        }
    }

    public static String readAvailable(InputStream stream) throws IOException {
        byte[] bytes = new byte[stream.available()];
        int n = stream.read(bytes);
        return new String(bytes, 0, n);
    }
}
