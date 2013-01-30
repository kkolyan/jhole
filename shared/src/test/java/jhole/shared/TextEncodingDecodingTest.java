package jhole.shared;

import jhole.streamcoding.StreamDecoder;
import jhole.streamcoding.StreamEncoder;
import jhole.streamcoding.TextStreamDecoder;
import jhole.streamcoding.TextStreamEncoder;

import java.io.*;
import java.util.Scanner;

public class TextEncodingDecodingTest extends EncodingDecodingTest {
    @Override
    protected StreamEncoder newStreamEncoder(ByteArrayOutputStream buf) {
        try {
            return new TextStreamEncoder(new PrintStream(buf, true, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected StreamDecoder newStreamDecoder(ByteArrayOutputStream buf) {
        return new TextStreamDecoder(new Scanner(buf.toString()));
    }
}
