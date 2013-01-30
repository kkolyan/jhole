package jhole.streamcoding;

import java.io.IOException;

public class MessageEncoderAdapter implements MessageEncoder {
    @Override
    public MessageEncoder addString(String s) {
        return this;
    }

    @Override
    public MessageEncoder addInt(int x) {
        return this;
    }

    @Override
    public MessageEncoder addLong(long x) {
        return this;
    }

    @Override
    public MessageEncoder addEnum(Enum<?> x) {
        return this;
    }

    @Override
    public MessageEncoder addBytes(byte[] bytes, int offset, int length) {
        return this;
    }

    @Override
    public MessageEncoder addBytes(byte[] bytes) {
        return this;
    }

    @Override
    public void commit() throws IOException {
    }
}
