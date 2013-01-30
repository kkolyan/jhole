package jhole.streamcoding;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

public class BinaryStreamEncoder implements StreamEncoder {
    private WritableByteChannel output;

    public BinaryStreamEncoder(OutputStream output) {
        this.output = Channels.newChannel(output);
    }

    @Override
    public MessageEncoder newMessage() {
        return new BinaryMessageEncoder();
    }

    private class BinaryMessageEncoder implements MessageEncoder {
        private ByteBuffer buf;

        private BinaryMessageEncoder() {
            buf = ByteBuffer.allocate(32);
            buf.putInt(0);
        }

        private void ensureRemaining(int rem) {
            int cap = buf.capacity();
            int reqCap = cap + rem;
            while (cap < reqCap) {
                cap *= 2;
            }
            byte[] bytes = Arrays.copyOf(buf.array(), cap);
            ByteBuffer nb = ByteBuffer.wrap(bytes);
            nb.position(buf.position());
            buf = nb;
        }

        @Override
        public BinaryMessageEncoder addString(String s) {
            if (s.trim().isEmpty() || s.contains(" ")) {
                throw new IllegalArgumentException(s);
            }
            byte[] bytes;
            try {
                bytes = s.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
            addBytes(bytes, 0, bytes.length);
            return this;
        }

        @Override
        public BinaryMessageEncoder addInt(int x) {
            ensureRemaining(4);
            buf.putInt(x);
            return this;
        }

        @Override
        public BinaryMessageEncoder addLong(long x) {
            ensureRemaining(8);
            buf.putLong(x);
            return this;
        }

        @Override
        public MessageEncoder addEnum(Enum<?> x) {
            return addString(x.name());
        }

        @Override
        public BinaryMessageEncoder addBytes(byte[] bytes, int offset, int length) {
            ensureRemaining(4+length);
            buf.putInt(length);
            buf.put(bytes, offset, length);
            return this;
        }

        @Override
        public BinaryMessageEncoder addBytes(byte[] bytes) {
            return addBytes(bytes, 0, bytes.length);
        }

        @Override
        public void commit() throws IOException {
            buf.putInt(0, buf.position() - 4);
            buf.flip();

            output.write(buf);
        }

        @Override
        public String toString() {
            return new String(buf.array());
        }
    }
}
