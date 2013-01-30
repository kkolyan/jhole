package jhole.streamcoding;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.util.Scanner;
import java.util.StringTokenizer;

public class TextStreamDecoder implements StreamDecoder {
    private Scanner scanner;

    public TextStreamDecoder(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    @Override
    public MessageDecoder next() {
        String line = scanner.nextLine();
        return new StreamMessageDecoder(line.trim());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private static class StreamMessageDecoder implements MessageDecoder {
        private StringTokenizer tokenizer;
        private String line;

        private StreamMessageDecoder(String line) {
            this.line = line;
            this.tokenizer = new StringTokenizer(line);
        }

        @Override
        public String nextString() {
            return tokenizer.nextToken().trim();
        }

        @Override
        public int nextInt() {
            return Integer.parseInt(nextString());
        }

        @Override
        public long nextLong() {
            return Long.parseLong(nextString());
        }

        @Override
        public <T extends Enum<T>> T nextEnum(Class<T> type) {
            String name = nextString();
            return Enum.valueOf(type, name);
        }

        @Override
        public byte[] nextBytes() {
            String s = nextString();
            if (s.equals(TextStreamEncoder.EMPTY)) {
                return new byte[0];
            }
            return Base64.decode(s);
        }

        @Override
        public boolean hasNext() {
            return tokenizer.hasMoreTokens();
        }

        @Override
        public StreamMessageDecoder copyOfInitialState() {
            return new StreamMessageDecoder(line);
        }

        @Override
        public String toString() {
            return line;
        }
    }
}
