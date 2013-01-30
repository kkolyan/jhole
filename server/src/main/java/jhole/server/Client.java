package jhole.server;

import jhole.streamcoding.TransferMode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface Client {
    void handleUpstream(HttpServletRequest request, TransferMode transferMode) throws IOException;
    void handleDownstream(HttpServletResponse response, TransferMode transferMode, long timeout) throws IOException;
    void destroy();
}
