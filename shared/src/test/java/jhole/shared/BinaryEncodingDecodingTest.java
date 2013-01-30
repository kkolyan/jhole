package jhole.shared;

import jhole.streamcoding.BinaryStreamDecoder;
import jhole.streamcoding.BinaryStreamEncoder;
import jhole.streamcoding.StreamDecoder;
import jhole.streamcoding.StreamEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BinaryEncodingDecodingTest extends EncodingDecodingTest {

    @Override
    protected StreamEncoder newStreamEncoder(ByteArrayOutputStream buf) {
        return new BinaryStreamEncoder(buf);
    }

    @Override
    protected StreamDecoder newStreamDecoder(ByteArrayOutputStream buf) {
        return new BinaryStreamDecoder(new ByteArrayInputStream(buf.toByteArray()));
    }

}
