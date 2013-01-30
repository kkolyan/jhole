package jhole.streamcoding;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.NoSuchElementException;

public class BinaryStreamDecoder implements StreamDecoder {
    private ReadableByteChannel input;
    private MessageDecoder next;
    private boolean needToCheck = true;

    public BinaryStreamDecoder(InputStream input) {
        this.input = Channels.newChannel(input);
    }

    @Override
    public boolean hasNext() {
        if (needToCheck) {
            next = readNext();
            needToCheck = false;
        }
        return next != null;
    }

    @Override
    public MessageDecoder next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        needToCheck = true;
        return next;
    }

    private MessageDecoder readNext() {
        ByteBuffer header = read(4);
        if (header == null) {
            return null;
        }
        int messageLength = header.getInt();
        ByteBuffer data = read(messageLength);
        if (data == null) {
            return null;
        }
        return new BinaryMessageDecoder(data);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private ByteBuffer read(int count) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(count);
            int rem = count;
            while (rem > 0) {
                int n = input.read(buffer);
                if (n < 0) {
                    return null;
                }
                rem -= n;
            }
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class BinaryMessageDecoder implements MessageDecoder {
        private ByteBuffer buf;
        private ByteBuffer initialState;

        private BinaryMessageDecoder(ByteBuffer buf) {
            this.buf = buf;
            initialState = buf.duplicate();
        }

        @Override
        public String nextString() {
            try {
                return new String(nextBytes(), "utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public int nextInt() {
            return buf.getInt();
        }

        @Override
        public long nextLong() {
            return buf.getLong();
        }

        @Override
        public <T extends Enum<T>> T nextEnum(Class<T> type) {
//            int ordinal = nextInt();
//            return type.getEnumConstants()[ordinal];
            String name = nextString();
            return Enum.valueOf(type, name);
        }

        @Override
        public byte[] nextBytes() {
            int n = buf.getInt();
            byte[] bytes = new byte[n];
            buf.get(bytes);
            return bytes;
        }

        @Override
        public boolean hasNext() {
            return buf.hasRemaining();
        }

        @Override
        public BinaryMessageDecoder copyOfInitialState() {
            return new BinaryMessageDecoder(initialState);
        }

        @Override
        public String toString() {
            return new String(buf.array());
        }
    }
}
