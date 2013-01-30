package jhole.streamcoding;

import java.io.IOException;

public interface MessageEncoder {
    MessageEncoder addString(String s);
    MessageEncoder addInt(int x);
    MessageEncoder addLong(long x);
    MessageEncoder addEnum(Enum<?> x);
    MessageEncoder addBytes(byte[] bytes, int offset, int length);
    MessageEncoder addBytes(byte[] bytes);
    void commit() throws IOException;
}
