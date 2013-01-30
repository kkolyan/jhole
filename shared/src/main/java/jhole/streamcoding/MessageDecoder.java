package jhole.streamcoding;

public interface MessageDecoder {
    String nextString();
    int nextInt();
    long nextLong();
    <T extends Enum<T>> T nextEnum(Class<T> type);
    byte[] nextBytes();
    boolean hasNext();
    MessageDecoder copyOfInitialState();
}
