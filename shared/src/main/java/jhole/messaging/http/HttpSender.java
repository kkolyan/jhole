package jhole.messaging.http;

import jhole.streamcoding.Codec;
import jhole.streamcoding.ObjectStream;
import jhole.streamcoding.TransferModeBasedCodec;
import jhole.streamcoding.TransferMode;
import org.apache.commons.httpclient.ChunkedOutputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class HttpSender implements Runnable, RequestEntity {
    private static final Logger logger = LoggerFactory.getLogger(HttpSender.class);
    private HttpClient client;
    private String url;
    private BlockingQueue<Object> messages = new ArrayBlockingQueue<Object>(1024*1024);
    private volatile boolean stopped;
    private TransferMode transferMode;

    public HttpSender(HttpClient client, String url, TransferMode transferMode) {
        this.client = client;
        this.url = url;
        this.transferMode = transferMode;
    }

    @Override
    public void run() {
        try {
            while (!stopped) {
                try {
                    PostMethod post = new PostMethod(url+"?transferMode="+
                            System.getProperty("jhole.client.transferMode")+"&direction=upstream");
                    post.setRequestEntity(this);

                    logger.info("sending request");
                    try {
                        int status = client.executeMethod(post);
                        if (status / 100 != 2) {
                            logger.warn(""+post.getStatusLine());
                            Thread.sleep(1000);
                        }
                    } finally {
                        logger.info("releasing connection");
                        post.releaseConnection();
                    }

                } catch (Exception e) {
                    logger.error(e.toString(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString(), e);
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public void writeRequest(OutputStream out) throws IOException {
        try {
            Field cache = ChunkedOutputStream.class.getDeclaredField("cache");
            cache.setAccessible(true);
            cache.set(out, new byte[16]);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Codec codec = new TransferModeBasedCodec(transferMode);
        ObjectStream encoder = codec.write(out);

        while (true) {
            Object message;
            try {
                message = messages.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            if (message == null) {
                break;
            }
            encoder.write(message);
            logger.trace("sent: {}", message);
        }
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public String getContentType() {
        return transferMode.getContentType();
    }

    public void addMessage(Object message) {
        messages.offer(message);
    }

    public void stop() {
        stopped = true;
    }
}
