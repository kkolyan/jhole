package jhole.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class CountingOutputStream extends OutputStream {
    private final AtomicLong count;

    public CountingOutputStream(AtomicLong count) {
        this.count = count;
    }

    @Override
    public void write(int b) throws IOException {
        count.addAndGet(1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        count.addAndGet(b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        count.addAndGet(len);
    }

    public long getCount() {
        return count.get();
    }
}
