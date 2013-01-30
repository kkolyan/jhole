package jhole.client;

import jhole.history.HistoryEntry;
import jhole.history.HistorySection;
import jhole.utils.LineReader;
import jhole.utils.StreamUtils;
import jhole.history.History;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.Executor;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class Console implements Runnable {

    private static ServerSocket serverSocket;
    private static int port;

    public static void launch(Executor executor) throws IOException {
        port = Integer.getInteger("jhole.client.console.port");
        serverSocket = new ServerSocket(port);
        Properties props = new Properties();
        props.load(Console.class.getClassLoader().getResourceAsStream("velocity.properties"));
        Velocity.init(props);
        executor.execute(new Console());
    }

    public static int getPort() {
        return port;
    }

    private Console() {
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                LineReader reader = new LineReader(socket.getInputStream());
                PrintStream writer = new PrintStream(socket.getOutputStream(), true, "utf8");

                String status = "200 OK";
                Object content = null;
                Map<String,String> headers = new TreeMap<String, String>();

                HttpRequest request = HttpRequest.parseRequest(reader);
                try {
                    if (request.getAddress().startsWith("/history/")) {
                        String s = request.getAddress().substring("/history/".length());
                        if (s.contains(".")) {
                            s = s.substring(0, s.lastIndexOf("."));
                        }
                        String path[] = s.split("/");
                        HistorySection section = History.getInstance().getEntries()
                                .get(Integer.parseInt(path[0]))
                                .getSection(path[1]);
                        if (section == null) {
                            status = "404 Not Found";
                        } else {
                            content = section.getContent();
                            headers.put("Content-Type", "text/plain");
                        }
                    }
                    else if (request.getAddress().endsWith(".vm")) {
                        try {
                            Template template = Velocity.getTemplate("history.vm");

                            StringWriter s = new StringWriter();
                            Context context = new VelocityContext();
                            context.put("history", History.getInstance());
                            template.merge(context, s);
                            content = s;
                            headers.put("Content-Type", "text/html");
                        } catch (ResourceNotFoundException e) {
                            status = "404 Not Found";
                        }
                    }
                    else {
                        InputStream resource = getClass().getClassLoader().getResourceAsStream("."+request.getAddress());
                        if (resource == null) {
                            status = "404 Not Found";
                        }
                        else {
                            ByteArrayOutputStream buf = new ByteArrayOutputStream();
                            StreamUtils.pump(resource, buf);
                            content = buf;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    status = "500 Internal Server Error";
                } finally {
                    writer.println("HTTP/1.1 "+status);
                    if (content instanceof ByteArrayOutputStream) {
                        headers.put("Content-Length", ((ByteArrayOutputStream)content).size()+"");
                    }
                    if (content instanceof String) {
                        headers.put("Content-Length", ((String)content).getBytes().length+"");
                    }
                    for (Map.Entry<String, String> header: headers.entrySet()) {
                        writer.print(header.getKey());
                        writer.print(": ");
                        writer.print(header.getValue());
                        writer.println();
                    }
                    writer.println();
                    if (content != null) {
                        writer.print(content);
                    }
                    writer.flush();
                    socket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
