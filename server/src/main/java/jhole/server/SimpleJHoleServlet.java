package jhole.server;


import jhole.messaging.direct.DirectMessagingService;
import jhole.streamcoding.TransferMode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleJHoleServlet extends HttpServlet {

    private ExecutorService executor = Executors.newCachedThreadPool();
    private ClientManager clientManager;

    @Override
    public void init() throws ServletException {
        clientManager = new ClientManager(new DirectMessagingService(executor));
        getServletContext().setAttribute("clientManager", clientManager);
    }

    @Override
    public void destroy() {
        clientManager.destroy();
        executor.shutdown();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getPathInfo() == null) {
            String id = clientManager.create();
            resp.sendRedirect(req.getContextPath() + req.getServletPath() + "/" + id);
        }
        else {

            String id = req.getPathInfo().substring(1);
            Client client = clientManager.get(id);
            if (client == null) {
                resp.sendError(404);
            } else {

                String t = req.getParameter("transferMode");
                t = (t != null) ? t : "text";
                TransferMode transferMode = TransferMode.valueOf(t.toUpperCase());

                t = req.getParameter("timeout");
                t = (t != null) ? t : "1000";
                long timeout = Long.parseLong(t);

                String direction = req.getParameter("direction");
                if ("upstream".equals(direction)) {
                    client.handleUpstream(req, transferMode);
                }
                else if ("downstream".equals(direction)) {
                    client.handleDownstream(resp, transferMode, timeout);
                }
                else throw new IllegalStateException("unknown stream direction: "+direction);
            }
        }
    }
}
