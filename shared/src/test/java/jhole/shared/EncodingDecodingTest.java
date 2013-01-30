package jhole.shared;

import jhole.streamcoding.MessageDecoder;
import jhole.streamcoding.MessageEncoder;
import jhole.streamcoding.StreamDecoder;
import jhole.streamcoding.StreamEncoder;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public abstract class EncodingDecodingTest {
    protected abstract StreamEncoder newStreamEncoder(ByteArrayOutputStream buf);

    protected abstract StreamDecoder newStreamDecoder(ByteArrayOutputStream buf);

    @Test
    public void test() throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        StreamEncoder se = newStreamEncoder(buf);
        MessageEncoder me;

        me = se.newMessage();
        me.addString("Hello");
        me.addBytes("123".getBytes(), 0, 3);
        me.addInt(67);
        me.commit();

        me = se.newMessage();
        me.addString("Goodbye");
        me.addBytes("567".getBytes(), 0, 3);
        me.addInt(-890);
        me.addBytes("".getBytes());
        me.commit();

        assertNotSame(0, buf.size());

        StreamDecoder sd = newStreamDecoder(buf);
        MessageDecoder md;

        md = sd.next();
        assertEquals("Hello", md.nextString());
        assertEquals("123", new String(md.nextBytes()));
        assertEquals(67, md.nextInt());
        assertEquals(false, md.hasNext());

        md = sd.next();
        assertEquals("Goodbye", md.nextString());
        assertEquals("567", new String(md.nextBytes()));
        assertEquals(-890, md.nextInt());
        assertEquals(0, md.nextBytes().length);
        assertEquals(false, md.hasNext());

        assertEquals(false, sd.hasNext());

    }
}
