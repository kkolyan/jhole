package jhole.messaging.http;

import jhole.messaging.MessageHandler;
import jhole.messaging.MessagingService;
import jhole.messaging.Messenger;
import jhole.streamcoding.TransferMode;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executor;

public class HttpMessagingService implements MessagingService {
    private HttpClient client;
    private Executor executor;
    private TransferMode transferMode;

    public HttpMessagingService(Executor executor, TransferMode transferMode) {
        this.executor = executor;
        this.transferMode = transferMode;
        client = createHttpClient();
    }

    private HttpClient createHttpClient() {
        HttpClient client = new HttpClient();
        if (Boolean.getBoolean("jhole.server.auth")) {
            client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, Arrays.asList(AuthPolicy.DIGEST, AuthPolicy.BASIC));
            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                    System.getProperty("jhole.server.auth.username"),
                    System.getProperty("jhole.server.auth.password")
            ));
        }
        client.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
        return client;
    }

    @Override
    public Messenger join(final MessageHandler handler) {
        PostMethod getMethod = new PostMethod(System.getProperty("jhole.server.baseUrl"));
        String url;
        try {
            int status = client.executeMethod(getMethod);
            if (status / 100 != 3) {
                throw new IllegalStateException(getMethod.getStatusLine().toString());
            }
            url = getMethod.getResponseHeader("Location").getValue();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        final HttpSender sender = new HttpSender(createHttpClient(), url, transferMode);
        final HttpReceiver receiver = new HttpReceiver(createHttpClient(), handler, url, transferMode);

        executor.execute(sender);
        executor.execute(receiver);

        return new Messenger() {
            @Override
            public void send(Object message) {
                sender.addMessage(message);
            }

            @Override
            public void destroy() {
                receiver.stop();
                sender.stop();
            }
        };
    }
}
