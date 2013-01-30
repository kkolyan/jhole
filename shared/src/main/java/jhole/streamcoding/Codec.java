package jhole.streamcoding;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public interface Codec {

    Iterator<?> read(InputStream stream);

    ObjectStream write(OutputStream stream);

}
