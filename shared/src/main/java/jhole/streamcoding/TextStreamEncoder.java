package jhole.streamcoding;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.IOException;
import java.util.Arrays;

public class TextStreamEncoder implements StreamEncoder {

    public static final String EMPTY = "empty";
    private Appendable output;

    public TextStreamEncoder(Appendable output) {
        this.output = output;
    }

    @Override
    public MessageEncoder newMessage() {
        return new TextMessageEncoder();
    }

    private class TextMessageEncoder implements MessageEncoder {
        final StringBuilder buffer = new StringBuilder();

        @Override
        public TextMessageEncoder addString(String s) {
            if (s.trim().isEmpty() || s.contains(" ")) {
                throw new IllegalArgumentException(s);
            }
            if (buffer.length() > 0) {
                buffer.append(" ");
            }
            buffer.append(s);
            return this;
        }

        @Override
        public TextMessageEncoder addInt(int i) {
            return addString(i+"");
        }

        @Override
        public TextMessageEncoder addLong(long x) {
            return addString(x+"");
        }

        @Override
        public TextMessageEncoder addEnum(Enum<?> x) {
            return addString(x.name());
        }

        @Override
        public TextMessageEncoder addBytes(byte[] bytes, int offset, int length) {
            if (length == 0) {
                return addString(EMPTY);
            }
            return addString(Base64.encode(Arrays.copyOfRange(bytes, offset, offset+length)));
        }

        @Override
        public TextMessageEncoder addBytes(byte[] bytes) {
            return addBytes(bytes, 0, bytes.length);
        }

        @Override
        public void commit() throws IOException {
            output.append(buffer).append("\n");
        }
    }
}
