package jhole.streamcoding;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public enum  TransferMode {
    TEXT("text/plain") {
        @Override
        public StreamEncoder getStreamEncoder(OutputStream outputStream) {
            try {
                return new TextStreamEncoder(new PrintStream(outputStream, true, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public StreamDecoder getStreamDecoder(InputStream inputStream) {
            return new TextStreamDecoder(new Scanner(inputStream));
        }
    }, BINARY("application/octet-stream") {
        @Override
        public StreamDecoder getStreamDecoder(InputStream inputStream) {
            return new BinaryStreamDecoder(inputStream);
        }

        @Override
        public StreamEncoder getStreamEncoder(OutputStream outputStream) {
            return new BinaryStreamEncoder(outputStream);
        }
    };

    private String contentType;

    private TransferMode(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public abstract StreamDecoder getStreamDecoder(InputStream inputStream);
    public abstract StreamEncoder getStreamEncoder(OutputStream outputStream);

}
