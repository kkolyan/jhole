package jhole.streamcoding;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Iterator;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class TransferModeBasedCodec implements Codec {
    private TransferMode transferMode;

    public TransferModeBasedCodec(TransferMode transferMode) {
        this.transferMode = transferMode;
    }

    @Override
    public Iterator<?> read(InputStream stream) {
        final StreamDecoder streamDecoder = transferMode.getStreamDecoder(stream);
        return new Iterator<Object>() {
            @Override
            public boolean hasNext() {
                return streamDecoder.hasNext();
            }

            @Override
            public Object next() {
                try {
                    MessageDecoder decoder = streamDecoder.next();
                    String type = decoder.nextString();
                    Object o = Class.forName(type).newInstance();
                    for (Class c = o.getClass(); c != Object.class; c = c.getSuperclass()) {
                        for (Field field: c.getDeclaredFields()) {
                            try {
                                field.setAccessible(true);
                                if (field.getType() == int.class) {
                                    field.set(o, decoder.nextInt());
                                }
                                else if (field.getType() == long.class) {
                                    field.set(o, decoder.nextLong());
                                }
                                else if (field.getType() == byte[].class) {
                                    field.set(o, decoder.nextBytes());
                                }
                                else if (field.getType() == String.class) {
                                    field.set(o, decoder.nextString());
                                }
                                else throw new IllegalStateException("unsupported type of field: "+field);
                            } catch (Exception e) {
                                throw e;
                            }
                        }
                    }
                    return o;
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public ObjectStream write(OutputStream stream) {
        final StreamEncoder streamEncoder = transferMode.getStreamEncoder(stream);
        return new ObjectStream() {
            @Override
            public void write(Object o) {
                try {
                    MessageEncoder encoder = streamEncoder.newMessage();
                    encoder.addString(o.getClass().getName());
                    for (Class c = o.getClass(); c != Object.class; c = c.getSuperclass()) {
                        for (Field field: c.getDeclaredFields()) {
                            field.setAccessible(true);
                            if (field.getType() == int.class) {
                                encoder.addInt(field.getInt(o));
                            }
                            else if (field.getType() == long.class) {
                                encoder.addLong(field.getLong(o));
                            }
                            else if (field.getType() == byte[].class) {
                                encoder.addBytes((byte[]) field.get(o));
                            }
                            else if (field.getType() == String.class) {
                                encoder.addString((String) field.get(o));
                            }
                            else throw new IllegalStateException("unsupported type of field: "+field);
                        }
                    }
                    encoder.commit();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
