package jhole.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class ExceptionFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            logger.error(e.toString(), e);
            HttpServletResponse resp = (HttpServletResponse) response;
            try {
                e.printStackTrace(resp.getWriter());
            } catch (IllegalStateException e1) {
                e.printStackTrace((new PrintStream(resp.getOutputStream(), true, "utf-8")));
            }
            resp.setContentType("text/plain");
            resp.setStatus(500);
        }
    }

    @Override
    public void destroy() {
    }
}
