package jhole.messaging.http;

import jhole.streamcoding.Codec;
import jhole.streamcoding.TransferModeBasedCodec;
import jhole.messaging.MessageHandler;
import jhole.streamcoding.TransferMode;
import jhole.utils.Iterators;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpReceiver implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HttpReceiver.class);
    private HttpClient client;
    private MessageHandler messageHandler;
    private String url;
    private volatile boolean stopped;
    private TransferMode transferMode;

    public HttpReceiver(HttpClient client, MessageHandler messageHandler, String url, TransferMode transferMode) {
        this.client = client;
        this.messageHandler = messageHandler;
        this.url = url;
        this.transferMode = transferMode;
    }

    @Override
    public void run() {
        try {
            while (!stopped) {
                try {
                    PostMethod post = new PostMethod(url
                            +"?transferMode="
                            +System.getProperty("jhole.client.transferMode")
                            +"&timeout="
                            +System.getProperty("jhole.server.downstream.timeout")
                            +"&direction=downstream");

                    try {
                        logger.info("sending request");
                        int status = client.executeMethod(post);
                        if (status / 100 != 2) {
                            logger.warn(""+post.getStatusLine());
                            Thread.sleep(1000);
                        } else {
                            Codec codec = new TransferModeBasedCodec(transferMode);

                            for (Object message: Iterators.iterateOnce(codec.read(post.getResponseBodyAsStream()))) {
                                logger.trace("received: {}", message);
                                messageHandler.handleMessage(message);
                            }
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

    public void stop() {
        stopped = true;
    }

}
